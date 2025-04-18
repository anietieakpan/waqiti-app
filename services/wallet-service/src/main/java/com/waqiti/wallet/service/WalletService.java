/**
 * File: ./wallet-service/src/main/java/com/waqiti/wallet/service/EnhancedWalletService.java
 */
package com.waqiti.wallet.service;

import com.waqiti.wallet.domain.*;
import com.waqiti.wallet.dto.*;
import com.waqiti.wallet.domain.*;
import com.waqiti.wallet.dto.*;
import com.waqiti.wallet.repository.TransactionRepository;
import com.waqiti.wallet.repository.WalletRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Enhanced implementation of the WalletService with improved transaction management
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class WalletService {
    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final IntegrationService integrationService;
    private final TransactionLogger transactionLogger;

    private static final String SYSTEM_USER = "SYSTEM";

    /**
     * Creates a new wallet
     */
    @Transactional
    public WalletResponse createWallet(CreateWalletRequest request) {
        log.info("Creating new wallet for user: {}, type: {}, currency: {}",
                request.getUserId(), request.getWalletType(), request.getCurrency());

        // Check if user already has a wallet in this currency
        Optional<Wallet> existingWallet = walletRepository.findByUserIdAndCurrency(
                request.getUserId(), request.getCurrency());

        if (existingWallet.isPresent()) {
            log.warn("User already has a wallet in currency: {}", request.getCurrency());
            throw new IllegalStateException(
                    "User already has a wallet in currency: " + request.getCurrency());
        }

        // Create the wallet in the external system
        String externalId = integrationService.createWallet(
                request.getUserId(),
                request.getWalletType(),
                request.getAccountType(),
                request.getCurrency());

        // Create the wallet in our system
        Wallet wallet = Wallet.create(
                request.getUserId(),
                externalId,
                request.getWalletType(),
                request.getAccountType(),
                request.getCurrency());

        wallet.setCreatedBy(SYSTEM_USER);
        wallet = walletRepository.save(wallet);

        // Log wallet creation
        transactionLogger.logWalletEvent(
                wallet.getUserId(),
                wallet.getId(),
                "WALLET_CREATED",
                wallet.getBalance(),
                wallet.getCurrency(),
                null);

        return mapToWalletResponse(wallet);
    }

    /**
     * Gets a wallet by ID
     */
    @Transactional(readOnly = true)
    public WalletResponse getWallet(UUID walletId) {
        log.info("Getting wallet: {}", walletId);

        Wallet wallet = getWalletEntity(walletId);

        // Update balance from external system
        BigDecimal remoteBalance = integrationService.getWalletBalance(wallet);

        // Only update the balance if it has changed
        if (wallet.getBalance().compareTo(remoteBalance) != 0) {
            log.info("Wallet balance updated from {} to {}", wallet.getBalance(), remoteBalance);
            wallet.updateBalance(remoteBalance);
            walletRepository.save(wallet);
        }

        return mapToWalletResponse(wallet);
    }

    /**
     * Gets all wallets for a user
     */
    @Transactional(readOnly = true)
    public List<WalletResponse> getUserWallets(UUID userId) {
        log.info("Getting wallets for user: {}", userId);

        List<Wallet> wallets = walletRepository.findByUserId(userId);

        // Update balances from external system in parallel




        wallets.parallelStream().forEach(wallet -> {
            try {
                BigDecimal remoteBalance = integrationService.getWalletBalance(wallet);
                if (wallet.getBalance().compareTo(remoteBalance) != 0) {
                    log.info("Wallet {} balance updated from {} to {}",
                            wallet.getId(), wallet.getBalance(), remoteBalance);
                    wallet.updateBalance(remoteBalance);
                    walletRepository.save(wallet);
                }
            } catch (Exception e) {
                log.error("Failed to update wallet balance: {}", wallet.getId(), e);
            }
        });

        return wallets.stream()
                .map(this::mapToWalletResponse)
                .collect(Collectors.toList());
    }

    /**
     * Transfers money between wallets with optimistic locking
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TransactionResponse transfer(TransferRequest request) {
        log.info("Transferring {} from wallet {} to wallet {}",
                request.getAmount(), request.getSourceWalletId(), request.getTargetWalletId());

        // Lock source wallet first to prevent deadlocks
        Wallet sourceWallet = walletRepository.findByIdWithLock(request.getSourceWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Source wallet not found: " +
                        request.getSourceWalletId()));

        Wallet targetWallet = walletRepository.findByIdWithLock(request.getTargetWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Target wallet not found: " +
                        request.getTargetWalletId()));

        // Validate wallets
        validateWalletForTransfer(sourceWallet);
        validateWalletForTransfer(targetWallet);

        // Validate currencies match
        if (!sourceWallet.getCurrency().equals(targetWallet.getCurrency())) {
            throw new IllegalArgumentException(
                    "Currency mismatch: source wallet currency is " + sourceWallet.getCurrency() +
                            ", target wallet currency is " + targetWallet.getCurrency());
        }

        // Create transaction record
        Transaction transaction = transactionLogger.createTransactionAudit(
                sourceWallet.getId(),
                targetWallet.getId(),
                request.getAmount(),
                sourceWallet.getCurrency(),
                TransactionType.TRANSFER,
                request.getDescription());

        transaction.setCreatedBy(SYSTEM_USER);
        transaction = transactionRepository.save(transaction);

        try {
            // Mark transaction as in progress
            transaction.markInProgress();
            transaction = transactionRepository.save(transaction);

            // First check if source wallet has sufficient balance in our system
            if (sourceWallet.getBalance().compareTo(request.getAmount()) < 0) {
                // Try to update balance from external system
                BigDecimal remoteBalance = integrationService.getWalletBalance(sourceWallet);
                sourceWallet.updateBalance(remoteBalance);
                walletRepository.save(sourceWallet);

                // Check again
                if (sourceWallet.getBalance().compareTo(request.getAmount()) < 0) {
                    throw new InsufficientBalanceException(
                            "Insufficient balance: " + sourceWallet.getBalance() +
                                    " " + sourceWallet.getCurrency());
                }
            }

            // Perform transfer in external system
            String externalId = integrationService.transferBetweenWallets(
                    sourceWallet,
                    targetWallet,
                    request.getAmount());

            // Update wallet balances
            BigDecimal sourceBalance = integrationService.getWalletBalance(sourceWallet);
            BigDecimal targetBalance = integrationService.getWalletBalance(targetWallet);

            sourceWallet.updateBalance(sourceBalance);
            targetWallet.updateBalance(targetBalance);
            sourceWallet.setUpdatedBy(SYSTEM_USER);
            targetWallet.setUpdatedBy(SYSTEM_USER);

            walletRepository.save(sourceWallet);
            walletRepository.save(targetWallet);

            // Mark transaction as completed
            transaction.complete(externalId);
            transaction.setUpdatedBy(SYSTEM_USER);
            transaction = transactionRepository.save(transaction);

            // Log transaction
            transactionLogger.logTransaction(transaction);

            // Log wallet events for notification
            transactionLogger.logWalletEvent(
                    sourceWallet.getUserId(),
                    sourceWallet.getId(),
                    "TRANSFER_OUT",
                    request.getAmount(),
                    sourceWallet.getCurrency(),
                    transaction.getId());

            transactionLogger.logWalletEvent(
                    targetWallet.getUserId(),
                    targetWallet.getId(),
                    "TRANSFER_IN",
                    request.getAmount(),
                    targetWallet.getCurrency(),
                    transaction.getId());

            return mapToTransactionResponse(transaction);
        } catch (Exception e) {
            log.error("Transfer failed", e);

            // Mark transaction as failed
            transaction.fail(e.getMessage());
            transaction.setUpdatedBy(SYSTEM_USER);
            transactionRepository.save(transaction);

            // Log failure
            transactionLogger.logTransactionFailure(
                    transaction.getId(),
                    e.getMessage(),
                    e instanceof InsufficientBalanceException ? "INSUFFICIENT_FUNDS" : "TRANSFER_FAILED");

            if (e instanceof InsufficientBalanceException) {
                throw (InsufficientBalanceException) e;
            } else {
                throw new TransactionFailedException("Transfer failed: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Deposits money into a wallet
     */

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TransactionResponse deposit(DepositRequest request) {
        log.info("Depositing {} into wallet {}", request.getAmount(), request.getWalletId());

        Wallet wallet = walletRepository.findByIdWithLock(request.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " +
                        request.getWalletId()));

        // Validate wallet
        validateWalletForDeposit(wallet);

        // Create transaction record
        Transaction transaction = transactionLogger.createTransactionAudit(
                null,
                wallet.getId(),
                request.getAmount(),
                wallet.getCurrency(),
                TransactionType.DEPOSIT,
                request.getDescription());

        transaction.setCreatedBy(SYSTEM_USER);
        transaction = transactionRepository.save(transaction);

        try {
            // Mark transaction as in progress
            transaction.markInProgress();
            transaction = transactionRepository.save(transaction);

            // Perform deposit in external system
            String externalId = integrationService.depositToWallet(
                    wallet,
                    request.getAmount());

            // Update wallet balance
            BigDecimal newBalance = integrationService.getWalletBalance(wallet);
            wallet.updateBalance(newBalance);
            wallet.setUpdatedBy(SYSTEM_USER);
            walletRepository.save(wallet);

            // Mark transaction as completed
            transaction.complete(externalId);
            transaction.setUpdatedBy(SYSTEM_USER);
            transaction = transactionRepository.save(transaction);

            // Log transaction
            transactionLogger.logTransaction(transaction);

            // Log wallet event for notification
            transactionLogger.logWalletEvent(
                    wallet.getUserId(),
                    wallet.getId(),
                    "DEPOSIT",
                    request.getAmount(),
                    wallet.getCurrency(),
                    transaction.getId());

            return mapToTransactionResponse(transaction);
        } catch (Exception e) {
            log.error("Deposit failed", e);

            // Mark transaction as failed
            transaction.fail(e.getMessage());
            transaction.setUpdatedBy(SYSTEM_USER);
            transactionRepository.save(transaction);

            // Log failure
            transactionLogger.logTransactionFailure(
                    transaction.getId(),
                    e.getMessage(),
                    "DEPOSIT_FAILED");

            throw new TransactionFailedException("Deposit failed: " + e.getMessage(), e);
        }
    }

    /**
     * Withdraws money from a wallet
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public TransactionResponse withdraw(WithdrawalRequest request) {
        log.info("Withdrawing {} from wallet {}", request.getAmount(), request.getWalletId());

        Wallet wallet = walletRepository.findByIdWithLock(request.getWalletId())
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found: " +
                        request.getWalletId()));

        // Validate wallet
        validateWalletForWithdrawal(wallet);

        // Create transaction record
        Transaction transaction = transactionLogger.createTransactionAudit(
                wallet.getId(),
                null,
                request.getAmount(),
                wallet.getCurrency(),
                TransactionType.WITHDRAWAL,
                request.getDescription());

        transaction.setCreatedBy(SYSTEM_USER);
        transaction = transactionRepository.save(transaction);

        try {
            // Mark transaction as in progress
            transaction.markInProgress();
            transaction = transactionRepository.save(transaction);

            // First check if wallet has sufficient balance in our system
            if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
                // Try to update balance from external system
                BigDecimal remoteBalance = integrationService.getWalletBalance(wallet);
                wallet.updateBalance(remoteBalance);
                walletRepository.save(wallet);

                // Check again
                if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
                    throw new InsufficientBalanceException(
                            "Insufficient balance: " + wallet.getBalance() +
                                    " " + wallet.getCurrency());
                }
            }

            // Perform withdrawal in external system
            String externalId = integrationService.withdrawFromWallet(
                    wallet,
                    request.getAmount());

            // Update wallet balance
            BigDecimal newBalance = integrationService.getWalletBalance(wallet);
            wallet.updateBalance(newBalance);
            wallet.setUpdatedBy(SYSTEM_USER);
            walletRepository.save(wallet);

            // Mark transaction as completed
            transaction.complete(externalId);
            transaction.setUpdatedBy(SYSTEM_USER);
            transaction = transactionRepository.save(transaction);

            // Log transaction
            transactionLogger.logTransaction(transaction);

            // Log wallet event for notification
            transactionLogger.logWalletEvent(
                    wallet.getUserId(),
                    wallet.getId(),
                    "WITHDRAWAL",
                    request.getAmount(),
                    wallet.getCurrency(),
                    transaction.getId());

            return mapToTransactionResponse(transaction);
        } catch (Exception e) {
            log.error("Withdrawal failed", e);

            // Mark transaction as failed
            transaction.fail(e.getMessage());
            transaction.setUpdatedBy(SYSTEM_USER);
            transactionRepository.save(transaction);

            // Log failure
            transactionLogger.logTransactionFailure(
                    transaction.getId(),
                    e.getMessage(),
                    e instanceof InsufficientBalanceException ? "INSUFFICIENT_FUNDS" : "WITHDRAWAL_FAILED");

            if (e instanceof InsufficientBalanceException) {
                throw (InsufficientBalanceException) e;
            } else {
                throw new TransactionFailedException("Withdrawal failed: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Gets transactions for a wallet
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getWalletTransactions(UUID walletId, Pageable pageable) {
        log.info("Getting transactions for wallet: {}", walletId);

        // Ensure the wallet exists
        getWalletEntity(walletId);

        Page<Transaction> transactions = transactionRepository.findByWalletId(walletId, pageable);

        return transactions.map(this::mapToTransactionResponse);
    }

    /**
     * Gets transactions for a user
     */
    @Transactional(readOnly = true)
    public Page<TransactionResponse> getUserTransactions(UUID userId, Pageable pageable) {
        log.info("Getting transactions for user: {}", userId);

        Page<Transaction> transactions = transactionRepository.findByUserId(userId, pageable);

        return transactions.map(this::mapToTransactionResponse);
    }

    /**
     * Freezes a wallet
     */
    @Transactional
    public WalletResponse freezeWallet(UUID walletId, String reason) {
        log.info("Freezing wallet: {}", walletId);

        Wallet wallet = getWalletEntity(walletId);
        wallet.freeze();
        wallet.setUpdatedBy(SYSTEM_USER);
        wallet = walletRepository.save(wallet);

        // Log wallet event for notification
        transactionLogger.logWalletEvent(
                wallet.getUserId(),
                wallet.getId(),
                "WALLET_FROZEN",
                wallet.getBalance(),
                wallet.getCurrency(),
                null);

        return mapToWalletResponse(wallet);
    }

    /**
     * Unfreezes a wallet
     */
    @Transactional
    public WalletResponse unfreezeWallet(UUID walletId, String reason) {
        log.info("Unfreezing wallet: {}", walletId);

        Wallet wallet = getWalletEntity(walletId);
        wallet.unfreeze();
        wallet.setUpdatedBy(SYSTEM_USER);
        wallet = walletRepository.save(wallet);

        // Log wallet event for notification
        transactionLogger.logWalletEvent(
                wallet.getUserId(),
                wallet.getId(),
                "WALLET_UNFROZEN",
                wallet.getBalance(),
                wallet.getCurrency(),
                null);

        return mapToWalletResponse(wallet);
    }

    /**
     * Closes a wallet
     */
    @Transactional
    public WalletResponse closeWallet(UUID walletId, String reason) {
        log.info("Closing wallet: {}", walletId);

        Wallet wallet = getWalletEntity(walletId);
        wallet.close();
        wallet.setUpdatedBy(SYSTEM_USER);
        wallet = walletRepository.save(wallet);

        // Log wallet event for notification
        transactionLogger.logWalletEvent(
                wallet.getUserId(),
                wallet.getId(),
                "WALLET_CLOSED",
                wallet.getBalance(),
                wallet.getCurrency(),
                null);

        return mapToWalletResponse(wallet);
    }

    /**
     * Helper method to get a wallet entity by ID
     */
    private Wallet getWalletEntity(UUID walletId) {
        return walletRepository.findById(walletId)
                .orElseThrow(() -> new WalletNotFoundException("Wallet not found with ID: " + walletId));
    }

    /**
     * Validates wallet for transfer operations
     */
    private void validateWalletForTransfer(Wallet wallet) {
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletNotActiveException("Wallet is not active: " + wallet.getId() +
                    ", status: " + wallet.getStatus());
        }
    }

    /**
     * Validates wallet for deposit operations
     */
    private void validateWalletForDeposit(Wallet wallet) {
        if (wallet.getStatus() != WalletStatus.ACTIVE && wallet.getStatus() != WalletStatus.FROZEN) {
            throw new WalletNotActiveException("Cannot deposit to a wallet with status: " +
                    wallet.getStatus());
        }
    }

    /**
     * Validates wallet for withdrawal operations
     */
    private void validateWalletForWithdrawal(Wallet wallet) {
        if (wallet.getStatus() != WalletStatus.ACTIVE) {
            throw new WalletNotActiveException("Cannot withdraw from a wallet with status: " +
                    wallet.getStatus());
        }
    }

    /**
     * Maps a Wallet entity to a WalletResponse DTO
     */
    private WalletResponse mapToWalletResponse(Wallet wallet) {
        return WalletResponse.builder()
                .id(wallet.getId())
                .userId(wallet.getUserId())
                .externalId(wallet.getExternalId())
                .walletType(wallet.getWalletType())
                .accountType(wallet.getAccountType())
                .balance(wallet.getBalance())
                .currency(wallet.getCurrency())
                .status(wallet.getStatus().toString())
                .createdAt(wallet.getCreatedAt())
                .updatedAt(wallet.getUpdatedAt())
                .build();
    }

    /**
     * Maps a Transaction entity to a TransactionResponse DTO
     */
    private TransactionResponse mapToTransactionResponse(Transaction transaction) {
        return TransactionResponse.builder()
                .id(transaction.getId())
                .externalId(transaction.getExternalId())
                .sourceWalletId(transaction.getSourceWalletId())
                .targetWalletId(transaction.getTargetWalletId())
                .amount(transaction.getAmount())
                .currency(transaction.getCurrency())
                .type(transaction.getType().toString())
                .status(transaction.getStatus().toString())
                .description(transaction.getDescription())
                .createdAt(transaction.getCreatedAt())
                .build();
    }
}