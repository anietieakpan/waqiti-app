package com.p2pfinance.common.exception;

/**
 * Base exception for all business-related exceptions
 */
public class BusinessException extends RuntimeException {
    public BusinessException(String message) {
        super(message);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause);
    }
}