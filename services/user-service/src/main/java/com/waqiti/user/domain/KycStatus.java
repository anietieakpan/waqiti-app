package com.waqiti.user.domain;

/**
 * Represents the possible states of KYC (Know Your Customer) verification
 */
public enum KycStatus {
    NOT_STARTED,      // KYC verification not yet started
    IN_PROGRESS,      // KYC verification is in progress
    PENDING_REVIEW,   // KYC verification is pending review
    APPROVED,         // KYC verification is approved
    REJECTED          // KYC verification is rejected
}
