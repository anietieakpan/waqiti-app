package com.p2pfinance.user.domain;

import java.util.UUID;



/**
 * Thrown when a user already exists with the same username or email
 */
public class UserAlreadyExistsException extends RuntimeException {
    public UserAlreadyExistsException(String message) {
        super(message);
    }
}
