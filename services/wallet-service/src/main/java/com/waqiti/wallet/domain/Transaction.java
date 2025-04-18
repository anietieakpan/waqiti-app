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
@Table(name = "transactions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String externalId;

    @Column(nullable = true)
    private UUID sourceWalletId;

    @Column(nullable = true)
    private UUID targetWalletId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionStatus status;

    @Column(length = 500)
    private String description;

    @Column(length = 100)
    private String referenceId;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // Audit fields
    @Setter
    private String createdBy;

    @Setter
    private String updatedBy;

    /**
     * Creates a new transaction
     */
    public static Transaction create(UUID sourceWalletId, UUID targetWalletId, BigDecimal amount,
            String currency, TransactionType type, String description) {
        Transaction transaction = new Transaction();
        transaction.sourceWalletId = sourceWalletId;
        transaction.targetWalletId = targetWalletId;
        transaction.amount = amount;
        transaction.currency = currency;
        transaction.type = type;
        transaction.description = description;
        transaction.status = TransactionStatus.PENDING;
        transaction.createdAt = LocalDateTime.now();
        transaction.updatedAt = LocalDateTime.now();
        return transaction;
    }

    /**
     * Creates a new deposit transaction
     */
    public static Transaction createDeposit(UUID targetWalletId, BigDecimal amount,
            String currency, String description) {
        return create(null, targetWalletId, amount, currency, TransactionType.DEPOSIT, description);
    }

    /**
     * Creates a new withdrawal transaction
     */
    public static Transaction createWithdrawal(UUID sourceWalletId, BigDecimal amount,
            String currency, String description) {
        return create(sourceWalletId, null, amount, currency, TransactionType.WITHDRAWAL, description);
    }

    /**
     * Creates a new transfer transaction
     */
    public static Transaction createTransfer(UUID sourceWalletId, UUID targetWalletId,
            BigDecimal amount, String currency, String description) {
        return create(sourceWalletId, targetWalletId, amount, currency, TransactionType.TRANSFER, description);
    }

    /**
     * Marks the transaction as completed
     */
    public void complete(String externalId) {
        this.externalId = externalId;
        this.status = TransactionStatus.COMPLETED;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marks the transaction as failed
     */
    public void fail(String reason) {
        this.status = TransactionStatus.FAILED;
        this.description = reason;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Marks the transaction as in progress
     */
    public void markInProgress() {
        this.status = TransactionStatus.IN_PROGRESS;
        this.updatedAt = LocalDateTime.now();
    }
}