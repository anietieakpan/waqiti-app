package com.waqiti.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID; /**
 * Response for scheduled payment operations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ScheduledPaymentResponse {
    private UUID id;
    private UUID senderId;
    private UUID recipientId;
    private UUID sourceWalletId;
    private BigDecimal amount;
    private String currency;
    private String description;
    private String status;
    private String frequency;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate nextExecutionDate;
    private LocalDate lastExecutionDate;
    private int totalExecutions;
    private int completedExecutions;
    private Integer maxExecutions;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    
    // Added user information (populated from User service)
    private String senderName;
    private String recipientName;
    
    // List of executions
    private List<ScheduledPaymentExecutionResponse> executions;
}
