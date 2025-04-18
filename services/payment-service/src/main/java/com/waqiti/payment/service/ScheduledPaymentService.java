package com.waqiti.payment.service;

import com.waqiti.payment.client.UserServiceClient;
import com.waqiti.payment.client.WalletServiceClient;
import com.waqiti.payment.client.dto.TransferRequest;
import com.waqiti.payment.client.dto.TransferResponse;
import com.waqiti.payment.client.dto.UserResponse;
import com.waqiti.payment.domain.*;
import com.waqiti.payment.dto.*;
import com.waqiti.payment.domain.PaymentFailedException;
import com.waqiti.payment.domain.ScheduledPayment;
import com.waqiti.payment.domain.ScheduledPaymentFrequency;
import com.waqiti.payment.domain.ScheduledPaymentNotFoundException;
import com.waqiti.payment.dto.*;
import com.waqiti.payment.repository.ScheduledPaymentRepository;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ScheduledPaymentService {
    private final ScheduledPaymentRepository scheduledPaymentRepository;
    private final WalletServiceClient walletClient;
    private final UserServiceClient userClient;

    /**
     * Creates a scheduled payment
     */
    @Transactional
    public ScheduledPaymentResponse createScheduledPayment(UUID userId, CreateScheduledPaymentRequest request) {
        log.info("Creating scheduled payment from user {} to user {}", userId, request.getRecipientId());
        
        ScheduledPaymentFrequency frequency;
        try {
            frequency = ScheduledPaymentFrequency.valueOf(request.getFrequency());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid payment frequency: " + request.getFrequency());
        }
        
        // Create the scheduled payment
        ScheduledPayment scheduledPayment = ScheduledPayment.create(
                userId,
                request.getRecipientId(),
                request.getSourceWalletId(),
                request.getAmount(),
                request.getCurrency(),
                request.getDescription(),
                frequency,
                request.getStartDate(),
                request.getEndDate(),
                request.getMaxExecutions()
        );
        
        scheduledPayment = scheduledPaymentRepository.save(scheduledPayment);
        
        return enrichWithUserInfo(mapToScheduledPaymentResponse(scheduledPayment));
    }

    /**
     * Gets a scheduled payment by ID
     */
    @Transactional(readOnly = true)
    public ScheduledPaymentResponse getScheduledPaymentById(UUID id) {
        log.info("Getting scheduled payment with ID: {}", id);
        
        ScheduledPayment scheduledPayment = scheduledPaymentRepository.findById(id)
                .orElseThrow(() -> new ScheduledPaymentNotFoundException(id));
        
        return enrichWithUserInfo(mapToScheduledPaymentResponse(scheduledPayment));
    }

    /**
     * Gets scheduled payments created by a user
     */
    @Transactional(readOnly = true)
    public Page<ScheduledPaymentResponse> getScheduledPaymentsBySender(UUID userId, Pageable pageable) {
        log.info("Getting scheduled payments from user: {}", userId);
        
        Page<ScheduledPayment> payments = scheduledPaymentRepository.findBySenderId(userId, pageable);
        
        return payments.map(this::mapToScheduledPaymentResponse)
                .map(this::enrichWithUserInfo);
    }

    /**
     * Gets scheduled payments sent to a user
     */
    @Transactional(readOnly = true)
    public Page<ScheduledPaymentResponse> getScheduledPaymentsByRecipient(UUID userId, Pageable pageable) {
        log.info("Getting scheduled payments to user: {}", userId);
        
        Page<ScheduledPayment> payments = scheduledPaymentRepository.findByRecipientId(userId, pageable);
        
        return payments.map(this::mapToScheduledPaymentResponse)
                .map(this::enrichWithUserInfo);
    }

    /**
     * Pauses a scheduled payment
     */
    @Transactional
    public ScheduledPaymentResponse pauseScheduledPayment(UUID userId, UUID paymentId, PauseScheduledPaymentRequest request) {
        log.info("Pausing scheduled payment with ID: {}", paymentId);
        
        ScheduledPayment scheduledPayment = scheduledPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new ScheduledPaymentNotFoundException(paymentId));
        
        // Verify user is the sender
        if (!scheduledPayment.getSenderId().equals(userId)) {
            throw new IllegalArgumentException("User is not the sender of this scheduled payment");
        }
        
        scheduledPayment.pause();
        scheduledPayment = scheduledPaymentRepository.save(scheduledPayment);
        
        return enrichWithUserInfo(mapToScheduledPaymentResponse(scheduledPayment));
    }

    /**
     * Resumes a scheduled payment
     */
    @Transactional
    public ScheduledPaymentResponse resumeScheduledPayment(UUID userId, UUID paymentId, ResumeScheduledPaymentRequest request) {
        log.info("Resuming scheduled payment with ID: {}", paymentId);
        
        ScheduledPayment scheduledPayment = scheduledPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new ScheduledPaymentNotFoundException(paymentId));
        
        // Verify user is the sender
        if (!scheduledPayment.getSenderId().equals(userId)) {
            throw new IllegalArgumentException("User is not the sender of this scheduled payment");
        }
        
        scheduledPayment.resume();
        scheduledPayment = scheduledPaymentRepository.save(scheduledPayment);
        
        return enrichWithUserInfo(mapToScheduledPaymentResponse(scheduledPayment));
    }

    /**
     * Cancels a scheduled payment
     */
    @Transactional
    public ScheduledPaymentResponse cancelScheduledPayment(UUID userId, UUID paymentId, CancelScheduledPaymentRequest request) {
        log.info("Canceling scheduled payment with ID: {}", paymentId);
        
        ScheduledPayment scheduledPayment = scheduledPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new ScheduledPaymentNotFoundException(paymentId));
        
        // Verify user is the sender
        if (!scheduledPayment.getSenderId().equals(userId)) {
            throw new IllegalArgumentException("User is not the sender of this scheduled payment");
        }
        
        scheduledPayment.cancel();
        scheduledPayment = scheduledPaymentRepository.save(scheduledPayment);
        
        return enrichWithUserInfo(mapToScheduledPaymentResponse(scheduledPayment));
    }

    /**
     * Updates a scheduled payment
     */
    @Transactional
    public ScheduledPaymentResponse updateScheduledPayment(UUID userId, UUID paymentId, UpdateScheduledPaymentRequest request) {
        log.info("Updating scheduled payment with ID: {}", paymentId);
        
        ScheduledPayment scheduledPayment = scheduledPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new ScheduledPaymentNotFoundException(paymentId));
        
        // Verify user is the sender
        if (!scheduledPayment.getSenderId().equals(userId)) {
            throw new IllegalArgumentException("User is not the sender of this scheduled payment");
        }
        
        // TODO: Implement update logic for scheduled payment
        
        return enrichWithUserInfo(mapToScheduledPaymentResponse(scheduledPayment));
    }

    /**
     * Executes a scheduled payment immediately (manual trigger)
     */
    @Transactional
    @CircuitBreaker(name = "walletService", fallbackMethod = "executeScheduledPaymentFallback")
    @Retry(name = "walletService")
    public ScheduledPaymentResponse executeScheduledPayment(UUID userId, UUID paymentId) {
        log.info("Manually executing scheduled payment with ID: {}", paymentId);
        
        ScheduledPayment scheduledPayment = scheduledPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new ScheduledPaymentNotFoundException(paymentId));
        
        // Verify user is the sender
        if (!scheduledPayment.getSenderId().equals(userId)) {
            throw new IllegalArgumentException("User is not the sender of this scheduled payment");
        }
        
        // Process the payment
        processScheduledPayment(scheduledPayment);
        
        return enrichWithUserInfo(mapToScheduledPaymentResponse(scheduledPayment));
    }

    /**
     * Fallback method for executing scheduled payments
     */
    private ScheduledPaymentResponse executeScheduledPaymentFallback(UUID userId, UUID paymentId, Exception e) {
        log.warn("Fallback for executeScheduledPayment executed: {}", e.getMessage());
        throw new PaymentFailedException("Payment service temporarily unavailable. Please try again later.");
    }

    /**
     * Scheduled task to process due scheduled payments
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    @Transactional
    public void processDueScheduledPayments() {
        log.info("Processing due scheduled payments");
        
        LocalDate today = LocalDate.now();
        List<ScheduledPayment> duePayments = scheduledPaymentRepository.findActivePaymentsDueForExecution(today);
        
        log.info("Found {} scheduled payments due for execution", duePayments.size());
        
        for (ScheduledPayment payment : duePayments) {
            try {
                processScheduledPayment(payment);
            } catch (Exception e) {
                log.error("Failed to process scheduled payment {}", payment.getId(), e);
                // Record the failure but continue with other payments
                payment.recordFailedExecution(e.getMessage());
                scheduledPaymentRepository.save(payment);
            }
        }
    }

    /**
     * Processes a single scheduled payment
     */
    private void processScheduledPayment(ScheduledPayment payment) {
        log.info("Processing scheduled payment: {}", payment.getId());
        
        // Execute the payment through the wallet service
        TransferRequest transferRequest = TransferRequest.builder()
                .sourceWalletId(payment.getSourceWalletId())
                .targetWalletId(null) // This will be resolved by the wallet service
                .amount(payment.getAmount())
                .description(payment.getDescription() != null ?
                        payment.getDescription() : "Scheduled payment")
                .build();
        
        try {
            TransferResponse transferResponse = walletClient.transfer(transferRequest);
            
            // Record successful execution
            payment.recordExecution(UUID.fromString(transferResponse.getId().toString()));
            scheduledPaymentRepository.save(payment);
            
            log.info("Successfully processed scheduled payment: {}", payment.getId());
        } catch (Exception e) {
            log.error("Failed to process scheduled payment", e);
            
            // Record failed execution
            payment.recordFailedExecution(e.getMessage());
            scheduledPaymentRepository.save(payment);
            
            throw new PaymentFailedException("Failed to process scheduled payment: " + e.getMessage(), e);
        }
    }

    /**
     * Maps a ScheduledPayment entity to a ScheduledPaymentResponse DTO
     */
    private ScheduledPaymentResponse mapToScheduledPaymentResponse(ScheduledPayment payment) {
        List<ScheduledPaymentExecutionResponse> executions = payment.getExecutions().stream()
                .map(execution -> ScheduledPaymentExecutionResponse.builder()
                        .id(execution.getId())
                        .transactionId(execution.getTransactionId())
                        .amount(execution.getAmount())
                        .currency(execution.getCurrency())
                        .status(execution.getStatus().toString())
                        .errorMessage(execution.getErrorMessage())
                        .executionDate(execution.getExecutionDate())
                        .build())
                .collect(Collectors.toList());
        
        return ScheduledPaymentResponse.builder()
                .id(payment.getId())
                .senderId(payment.getSenderId())
                .recipientId(payment.getRecipientId())
                .sourceWalletId(payment.getSourceWalletId())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .description(payment.getDescription())
                .status(payment.getStatus().toString())
                .frequency(payment.getFrequency().toString())
                .startDate(payment.getStartDate())
                .endDate(payment.getEndDate())
                .nextExecutionDate(payment.getNextExecutionDate())
                .lastExecutionDate(payment.getLastExecutionDate())
                .totalExecutions(payment.getTotalExecutions())
                .completedExecutions(payment.getCompletedExecutions())
                .maxExecutions(payment.getMaxExecutions())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .executions(executions)
                .build();
    }

    /**
     * Enriches a scheduled payment response with user information
     */
    private ScheduledPaymentResponse enrichWithUserInfo(ScheduledPaymentResponse response) {
        try {
            List<UUID> userIds = List.of(response.getSenderId(), response.getRecipientId());
            List<UserResponse> users = userClient.getUsers(userIds);
            
            Map<UUID, UserResponse> userMap = users.stream()
                    .collect(Collectors.toMap(UserResponse::getId, Function.identity()));
            
            UserResponse sender = userMap.get(response.getSenderId());
            UserResponse recipient = userMap.get(response.getRecipientId());
            
            if (sender != null) {
                response.setSenderName(sender.getDisplayName());
            }
            
            if (recipient != null) {
                response.setRecipientName(recipient.getDisplayName());
            }
        } catch (Exception e) {
            log.warn("Failed to enrich scheduled payment with user info", e);
            // Continue without user info
        }
        
        return response;
    }
}