package com.p2pfinance.payment.domain;

/**
 * Thrown when a payment operation is invalid
 */
public class InvalidPaymentOperationException extends RuntimeException {
    public InvalidPaymentOperationException(String message) {
        super(message);
    }
}
