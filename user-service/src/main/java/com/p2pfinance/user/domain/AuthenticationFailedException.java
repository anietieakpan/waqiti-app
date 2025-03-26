package com.p2pfinance.user.domain;

import java.util.UUID;



/**
 * Thrown when authentication fails
 */
public class AuthenticationFailedException extends RuntimeException {
    public AuthenticationFailedException(String message) {
        super(message);
    }

    public AuthenticationFailedException(String message, Throwable cause) {
        super(message, cause);
    }
}