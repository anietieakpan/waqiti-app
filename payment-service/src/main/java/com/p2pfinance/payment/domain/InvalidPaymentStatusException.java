package com.p2pfinance.payment.domain;

/**
 * Thrown when a payment operation is not allowed due to the status
 */
public class InvalidPaymentStatusException extends RuntimeException {
    public InvalidPaymentStatusException(String message) {
        super(message);
    }
}
