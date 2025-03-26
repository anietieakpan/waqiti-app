package com.p2pfinance.user.domain;

/**
 * Represents the possible states of a user account
 */
public enum UserStatus {
    PENDING,    // User has registered but not yet activated their account
    ACTIVE,     // User account is active and can perform operations
    SUSPENDED,  // User account is temporarily suspended
    CLOSED      // User account is permanently closed
}
