package com.p2pfinance.common.exception;

import java.util.UUID;

/**
 * Exception thrown when a resource is not found
 */
public class ResourceNotFoundException extends BusinessException {
    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s not found with %s: %s", resourceName, fieldName, fieldValue));
    }

    public ResourceNotFoundException(String resourceName, UUID id) {
        super(String.format("%s not found with ID: %s", resourceName, id));
    }
}