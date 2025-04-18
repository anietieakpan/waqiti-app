package com.waqiti.payment.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Request to update a scheduled payment
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdateScheduledPaymentRequest {
    @Positive(message = "Amount must be positive")
    private BigDecimal amount;
    
    @Size(max = 255, message = "Description cannot exceed 255 characters")
    private String description;
    
    @Future(message = "End date must be in the future")
    private LocalDate endDate;
    
    private Integer maxExecutions;
}