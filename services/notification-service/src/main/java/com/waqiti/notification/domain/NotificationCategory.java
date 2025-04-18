package com.waqiti.notification.domain;

/**
 * Represents the categories of notifications
 */
public enum NotificationCategory {
    ACCOUNT,            // Account-related notifications (registration, verification, etc.)
    TRANSACTION,        // Transaction-related notifications (deposits, withdrawals, transfers)
    PAYMENT_REQUEST,    // Payment request notifications
    SCHEDULED_PAYMENT,  // Scheduled payment notifications
    SPLIT_PAYMENT,      // Split payment notifications
    SECURITY            // Security-related notifications (login, password change, etc.)
}
