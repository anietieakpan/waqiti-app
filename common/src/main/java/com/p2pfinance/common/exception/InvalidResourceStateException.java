/**
 * File: ./common/src/main/java/com/p2pfinance/common/exception/InvalidResourceStateException.java
 */
package com.p2pfinance.common.exception;

/**
 * Exception thrown when an operation is attempted on a resource that is in an invalid state
 */
public class InvalidResourceStateException extends BusinessException {
    public InvalidResourceStateException(String message) {
        super(message);
    }

    public InvalidResourceStateException(String resourceName, String currentState, String requiredState) {
        super(String.format("%s is in invalid state: %s. Required state: %s",
                resourceName, currentState, requiredState));
    }

    public InvalidResourceStateException(String message, Throwable cause) {
        super(message, cause);
    }
}