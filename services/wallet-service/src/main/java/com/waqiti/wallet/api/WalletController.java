package com.waqiti.wallet.api;

import com.waqiti.wallet.dto.*;
import com.waqiti.wallet.dto.*;
import com.waqiti.wallet.service.WalletService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/wallets")
@RequiredArgsConstructor
@Slf4j
public class WalletController {
    private final WalletService walletService;

    @PostMapping
    public ResponseEntity<WalletResponse> createWallet(@Valid @RequestBody CreateWalletRequest request) {
        log.info("Creating wallet: {}", request);
        return ResponseEntity.ok(walletService.createWallet(request));
    }

    @GetMapping("/{walletId}")
    public ResponseEntity<WalletResponse> getWallet(@PathVariable UUID walletId) {
        log.info("Getting wallet: {}", walletId);
        return ResponseEntity.ok(walletService.getWallet(walletId));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<WalletResponse>> getUserWallets(@PathVariable UUID userId) {
        log.info("Getting wallets for user: {}", userId);
        return ResponseEntity.ok(walletService.getUserWallets(userId));
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        log.info("Transferring: {}", request);
        return ResponseEntity.ok(walletService.transfer(request));
    }

    @PostMapping("/deposit")
    public ResponseEntity<TransactionResponse> deposit(@Valid @RequestBody DepositRequest request) {
        log.info("Depositing: {}", request);
        return ResponseEntity.ok(walletService.deposit(request));
    }

    @PostMapping("/withdraw")
    public ResponseEntity<TransactionResponse> withdraw(@Valid @RequestBody WithdrawalRequest request) {
        log.info("Withdrawing: {}", request);
        return ResponseEntity.ok(walletService.withdraw(request));
    }

    @GetMapping("/{walletId}/transactions")
    public ResponseEntity<Page<TransactionResponse>> getWalletTransactions(
            @PathVariable UUID walletId,
            Pageable pageable) {
        log.info("Getting transactions for wallet: {}", walletId);
        return ResponseEntity.ok(walletService.getWalletTransactions(walletId, pageable));
    }

    @GetMapping("/transactions/user/{userId}")
    public ResponseEntity<Page<TransactionResponse>> getUserTransactions(
            @PathVariable UUID userId,
            Pageable pageable) {
        log.info("Getting transactions for user: {}", userId);
        return ResponseEntity.ok(walletService.getUserTransactions(userId, pageable));
    }
}