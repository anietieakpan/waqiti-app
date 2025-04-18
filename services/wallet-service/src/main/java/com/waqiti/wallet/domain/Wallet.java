package com.waqiti.wallet.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "wallets")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Wallet {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String externalId;

    @Column(nullable = false)
    private String walletType; // "FINERACT" or "CYCLOS"

    @Column(nullable = false)
    private String accountType; // "SAVINGS", "CHECKING", etc.

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletStatus status;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    private Long version;

    // Audit fields
    @Setter
    private String createdBy;

    @Setter
    private String updatedBy;

    /**
     * Creates a new wallet
     */
    public static Wallet create(UUID userId, String externalId, String walletType,
            String accountType, String currency) {
        Wallet wallet = new Wallet();
        wallet.userId = userId;
        wallet.externalId = externalId;
        wallet.walletType = walletType;
        wallet.accountType = accountType;
        wallet.balance = BigDecimal.ZERO;
        wallet.currency = currency;
        wallet.status = WalletStatus.ACTIVE;
        wallet.createdAt = LocalDateTime.now();
        wallet.updatedAt = LocalDateTime.now();
        return wallet;
    }

    /**
     * Updates the wallet balance
     */
    public void updateBalance(BigDecimal newBalance) {
        validateWalletActive();
        this.balance = newBalance;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Credit the wallet (add funds)
     */
    public void credit(BigDecimal amount) {
        validateWalletActive();
        validatePositiveAmount(amount);
        this.balance = this.balance.add(amount);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Debit the wallet (remove funds)
     */
    public void debit(BigDecimal amount) {
        validateWalletActive();
        validatePositiveAmount(amount);
        validateSufficientBalance(amount);
        this.balance = this.balance.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Freezes the wallet
     */
    public void freeze() {
        validateWalletActive();
        this.status = WalletStatus.FROZEN;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Unfreezes the wallet
     */
    public void unfreeze() {
        if (this.status != WalletStatus.FROZEN) {
            throw new IllegalStateException("Wallet is not frozen");
        }
        this.status = WalletStatus.ACTIVE;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Closes the wallet
     */
    public void close() {
        if (this.status == WalletStatus.CLOSED) {
            throw new IllegalStateException("Wallet is already closed");
        }

        if (this.balance.compareTo(BigDecimal.ZERO) > 0) {
            throw new IllegalStateException("Cannot close wallet with positive balance");
        }

        this.status = WalletStatus.CLOSED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Validates that the wallet is active
     */
    private void validateWalletActive() {
        if (this.status != WalletStatus.ACTIVE) {
            throw new WalletNotActiveException("Wallet is not active, current status: " + this.status);
        }
    }

    /**
     * Validates that the amount is positive
     */
    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be positive");
        }
    }

    /**
     * Validates that the wallet has sufficient balance for a debit operation
     */
    private void validateSufficientBalance(BigDecimal amount) {
        if (this.balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException(
                    String.format("Insufficient balance: required %s %s, available %s %s",
                            amount, this.currency, this.balance, this.currency));
        }
    }
}