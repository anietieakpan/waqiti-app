package com.p2pfinance.payment.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "scheduled_payment_executions")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ScheduledPaymentExecution {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "scheduled_payment_id", nullable = false)
    private ScheduledPayment scheduledPayment;

    @Column(name = "transaction_id")
    private UUID transactionId;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ScheduledPaymentExecutionStatus status;

    @Column(length = 500)
    private String errorMessage;

    @Column(nullable = false, name = "execution_date")
    private LocalDateTime executionDate;

    // Audit fields
    @Setter
    @Column(name = "created_by")
    private String createdBy;

    /**
     * Creates a new successful execution
     */
    public static ScheduledPaymentExecution create(ScheduledPayment scheduledPayment, 
                                                UUID transactionId, BigDecimal amount, String currency) {
        ScheduledPaymentExecution execution = new ScheduledPaymentExecution();
        execution.scheduledPayment = scheduledPayment;
        execution.transactionId = transactionId;
        execution.amount = amount;
        execution.currency = currency;
        execution.status = ScheduledPaymentExecutionStatus.COMPLETED;
        execution.executionDate = LocalDateTime.now();
        return execution;
    }

    /**
     * Creates a new failed execution
     */
    public static ScheduledPaymentExecution createFailed(ScheduledPayment scheduledPayment, 
                                                      BigDecimal amount, String currency, String errorMessage) {
        ScheduledPaymentExecution execution = new ScheduledPaymentExecution();
        execution.scheduledPayment = scheduledPayment;
        execution.amount = amount;
        execution.currency = currency;
        execution.status = ScheduledPaymentExecutionStatus.FAILED;
        execution.errorMessage = errorMessage;
        execution.executionDate = LocalDateTime.now();
        return execution;
    }
}