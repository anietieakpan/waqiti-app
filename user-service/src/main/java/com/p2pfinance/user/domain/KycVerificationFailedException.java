package com.p2pfinance.user.domain;

import java.util.UUID;

/**
 * Thrown when a user's KYC verification fails
 */
public class KycVerificationFailedException extends RuntimeException {
    public KycVerificationFailedException(String message) {
        super(message);
    }

    public KycVerificationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}