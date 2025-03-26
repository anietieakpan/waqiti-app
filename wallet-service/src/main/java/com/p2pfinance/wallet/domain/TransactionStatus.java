package com.p2pfinance.wallet.domain;

/**
 * Represents the possible states of a transaction
 */
public enum TransactionStatus {
    PENDING, // Transaction is created but not yet processed
    IN_PROGRESS, // Transaction is being processed
    COMPLETED, // Transaction has been successfully completed
    FAILED // Transaction failed to complete
}
