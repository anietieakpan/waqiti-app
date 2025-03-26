// File: wallet-service/src/test/java/com/p2pfinance/wallet/service/WalletServiceTest.java
package com.p2pfinance.wallet.service;

import com.p2pfinance.wallet.domain.*;
import com.p2pfinance.wallet.dto.CreateWalletRequest;
import com.p2pfinance.wallet.dto.TransferRequest;
import com.p2pfinance.wallet.dto.WalletResponse;
import com.p2pfinance.wallet.dto.TransactionResponse;
import com.p2pfinance.wallet.repository.WalletRepository;
import com.p2pfinance.wallet.repository.TransactionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;

// Use LENIENT mode for shared mocks
@ExtendWith(MockitoExtension.class)
public class WalletServiceTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private IntegrationService integrationService;

    @Mock
    private TransactionLogger transactionLogger;

    @InjectMocks
    private WalletService walletService;

    private UUID userId;
    private UUID sourceWalletId;
    private UUID targetWalletId;
    private Wallet sourceWallet;
    private Wallet targetWallet;
    private CreateWalletRequest createWalletRequest;
    private TransferRequest transferRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        sourceWalletId = UUID.randomUUID();
        targetWalletId = UUID.randomUUID();

        createWalletRequest = new CreateWalletRequest();
        createWalletRequest.setUserId(userId);
        createWalletRequest.setWalletType("FINERACT");
        createWalletRequest.setAccountType("SAVINGS");
        createWalletRequest.setCurrency("USD");

        // Create source wallet with factory method
        sourceWallet = Wallet.create(userId, "ext-123", "FINERACT", "SAVINGS", "USD");
        ReflectionTestUtils.setField(sourceWallet, "id", sourceWalletId);
        sourceWallet.updateBalance(new BigDecimal("1000.00"));

        // Create target wallet with factory method
        targetWallet = Wallet.create(UUID.randomUUID(), "ext-456", "FINERACT", "SAVINGS", "USD");
        ReflectionTestUtils.setField(targetWallet, "id", targetWalletId);
        targetWallet.updateBalance(new BigDecimal("500.00"));

        transferRequest = new TransferRequest();
        transferRequest.setSourceWalletId(sourceWalletId);
        transferRequest.setTargetWalletId(targetWalletId);
        transferRequest.setAmount(new BigDecimal("100.00"));
        transferRequest.setDescription("Test transfer");

        // Only keep minimal common setup for shared repositories
        lenient().when(walletRepository.save(any(Wallet.class))).thenAnswer(i -> i.getArgument(0));
        lenient().when(transactionRepository.save(any(Transaction.class))).thenAnswer(i -> i.getArgument(0));
    }

    @Test
    void testCreateWallet_Success() {
        // Arrange - specific test mocks
        String externalId = "ext-wallet-123";
        when(integrationService.createWallet(any(), any(), any(), any())).thenReturn(externalId);
        when(walletRepository.findByUserIdAndCurrency(userId, "USD")).thenReturn(Optional.empty());

        // Act
        WalletResponse response = walletService.createWallet(createWalletRequest);

        // Assert
        assertNotNull(response);
        assertEquals(userId, response.getUserId());
        assertEquals("USD", response.getCurrency());
        assertEquals("FINERACT", response.getWalletType());
        assertEquals(BigDecimal.ZERO, response.getBalance());

        verify(integrationService).createWallet(eq(userId), eq("FINERACT"), eq("SAVINGS"), eq("USD"));
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void testTransfer_Success() {
        // Arrange - specific test mocks
        String externalTransactionId = "ext-tx-123";

        // Create a Transaction using the factory method
        Transaction mockedTransaction = Transaction.createTransfer(
                sourceWalletId,
                targetWalletId,
                new BigDecimal("100.00"),
                "USD",
                "Test transfer"
        );
        UUID transactionId = UUID.randomUUID();
        ReflectionTestUtils.setField(mockedTransaction, "id", transactionId);

        // Setup specific mocks for this test
        when(transactionLogger.createTransactionAudit(
                eq(sourceWalletId),
                eq(targetWalletId),
                eq(new BigDecimal("100.00")),
                eq("USD"),
                eq(TransactionType.TRANSFER),
                eq("Test transfer")
        )).thenReturn(mockedTransaction);

        when(walletRepository.findByIdWithLock(sourceWalletId)).thenReturn(Optional.of(sourceWallet));
        when(walletRepository.findByIdWithLock(targetWalletId)).thenReturn(Optional.of(targetWallet));
        when(integrationService.transferBetweenWallets(any(), any(), any())).thenReturn(externalTransactionId);
        when(integrationService.getWalletBalance(sourceWallet)).thenReturn(new BigDecimal("900.00"));
        when(integrationService.getWalletBalance(targetWallet)).thenReturn(new BigDecimal("600.00"));

        doNothing().when(transactionLogger).logTransaction(any(Transaction.class));
        doNothing().when(transactionLogger).logWalletEvent(
                any(UUID.class), any(UUID.class), anyString(), any(BigDecimal.class), anyString(), any(UUID.class));

        // Act
        TransactionResponse response = walletService.transfer(transferRequest);

        // Assert
        assertNotNull(response);
        assertEquals(sourceWalletId, response.getSourceWalletId());
        assertEquals(targetWalletId, response.getTargetWalletId());
        assertEquals(new BigDecimal("100.00"), response.getAmount());
        assertEquals("USD", response.getCurrency());
        assertEquals("Test transfer", response.getDescription());

        verify(walletRepository).findByIdWithLock(sourceWalletId);
        verify(walletRepository).findByIdWithLock(targetWalletId);
        verify(integrationService).transferBetweenWallets(eq(sourceWallet), eq(targetWallet), eq(new BigDecimal("100.00")));
        verify(walletRepository, times(2)).save(any(Wallet.class));
        verify(transactionRepository, atLeastOnce()).save(any(Transaction.class));
        verify(transactionLogger).logTransaction(any(Transaction.class));
    }

    @Test
    void testTransfer_InsufficientFunds() {
        // Arrange - specific test mocks
        Wallet lowBalanceWallet = Wallet.create(userId, "ext-789", "FINERACT", "SAVINGS", "USD");
        UUID lowBalanceWalletId = UUID.randomUUID();
        ReflectionTestUtils.setField(lowBalanceWallet, "id", lowBalanceWalletId);
        lowBalanceWallet.updateBalance(new BigDecimal("50.00"));

        // Create a separate transaction for this test using factory method
        Transaction lowBalanceTransaction = Transaction.createTransfer(
                lowBalanceWalletId,
                targetWalletId,
                new BigDecimal("100.00"),
                "USD",
                "Test insufficient funds"
        );
        UUID lowBalanceTransactionId = UUID.randomUUID();
        ReflectionTestUtils.setField(lowBalanceTransaction, "id", lowBalanceTransactionId);

        // Create a transfer request with low balance source
        TransferRequest lowBalanceRequest = new TransferRequest();
        lowBalanceRequest.setSourceWalletId(lowBalanceWalletId);
        lowBalanceRequest.setTargetWalletId(targetWalletId);
        lowBalanceRequest.setAmount(new BigDecimal("100.00"));
        lowBalanceRequest.setDescription("Test insufficient funds");

        // Setup specific mocks for this test
        when(transactionLogger.createTransactionAudit(
                eq(lowBalanceWalletId),
                eq(targetWalletId),
                eq(new BigDecimal("100.00")),
                eq("USD"),
                eq(TransactionType.TRANSFER),
                eq("Test insufficient funds")
        )).thenReturn(lowBalanceTransaction);

        when(walletRepository.findByIdWithLock(lowBalanceWalletId)).thenReturn(Optional.of(lowBalanceWallet));
        when(walletRepository.findByIdWithLock(targetWalletId)).thenReturn(Optional.of(targetWallet));
        when(integrationService.getWalletBalance(lowBalanceWallet)).thenReturn(new BigDecimal("50.00"));

        doNothing().when(transactionLogger).logTransactionFailure(
                any(UUID.class), anyString(), eq("INSUFFICIENT_FUNDS"));

        // Act & Assert
        InsufficientBalanceException exception = assertThrows(InsufficientBalanceException.class, () -> {
            walletService.transfer(lowBalanceRequest);
        });

        verify(walletRepository).findByIdWithLock(lowBalanceWalletId);
        verify(walletRepository).findByIdWithLock(targetWalletId);
        verify(integrationService).getWalletBalance(lowBalanceWallet);
        verify(transactionRepository, atLeastOnce()).save(any(Transaction.class));
        verify(transactionLogger).logTransactionFailure(
                any(UUID.class), anyString(), eq("INSUFFICIENT_FUNDS"));
    }
}