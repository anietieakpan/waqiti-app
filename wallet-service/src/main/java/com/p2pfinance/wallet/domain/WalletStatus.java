package com.p2pfinance.wallet.domain;

/**
 * Represents the possible states of a wallet
 */
public enum WalletStatus {
    ACTIVE, // Wallet is active and can be used for transactions
    FROZEN, // Wallet is temporarily suspended
    CLOSED // Wallet is permanently closed
}