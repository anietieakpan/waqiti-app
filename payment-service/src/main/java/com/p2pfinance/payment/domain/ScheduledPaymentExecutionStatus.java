package com.p2pfinance.payment.domain;

/**
 * Represents the possible states of a scheduled payment execution
 */
public enum ScheduledPaymentExecutionStatus {
    COMPLETED,  // Execution completed successfully
    FAILED      // Execution failed
}