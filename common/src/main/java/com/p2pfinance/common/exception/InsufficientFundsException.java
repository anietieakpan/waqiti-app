package com.p2pfinance.common.exception;

import java.math.BigDecimal;

/**
 * Exception thrown when there are insufficient funds for an operation
 */
public class InsufficientFundsException extends BusinessException {
    public InsufficientFundsException(String message) {
        super(message);
    }

    public InsufficientFundsException(String message, Throwable cause) {
        super(message, cause);
    }

    public InsufficientFundsException(BigDecimal available, BigDecimal required, String currency) {
        super(String.format("Insufficient funds: available %s %s, required %s %s",
                available, currency, required, currency));
    }

    public InsufficientFundsException(BigDecimal available, BigDecimal required, String currency, Throwable cause) {
        super(String.format("Insufficient funds: available %s %s, required %s %s",
                available, currency, required, currency), cause);
    }

}