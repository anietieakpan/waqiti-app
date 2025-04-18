package com.waqiti.wallet.domain;

/**
 * Represents the possible types of transactions
 */
public enum TransactionType {
    DEPOSIT, // Money coming into a wallet from an external source
    WITHDRAWAL, // Money going out of a wallet to an external destination
    TRANSFER, // Money moving from one wallet to another
    PAYMENT, // Payment for goods or services
    REFUND, // Refund of a previous payment
    FEE // Service fee
}

