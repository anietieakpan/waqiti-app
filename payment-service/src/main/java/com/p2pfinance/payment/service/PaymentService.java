package com.p2pfinance.payment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p2pfinance.payment.client.UserServiceClient;
import com.p2pfinance.payment.client.WalletServiceClient;
import com.p2pfinance.payment.client.dto.TransferRequest;
import com.p2pfinance.payment.client.dto.TransferResponse;
import com.p2pfinance.payment.client.dto.UserResponse;
import com.p2pfinance.payment.client.dto.WalletResponse;
import com.p2pfinance.payment.domain.*;
import com.p2pfinance.payment.dto.*;
import com.p2pfinance.payment.repository.PaymentRequestRepository;
import io.github.resilience4j.bulkhead.annotation.Bulkhead;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class PaymentService {
    private final PaymentRequestRepository paymentRequestRepository;
    private final WalletServiceClient walletClient;
    private final UserServiceClient userClient;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final MeterRegistry meterRegistry;

    private static final String PAYMENT_EVENTS_TOPIC = "payment-request-events";
    private static final int DEFAULT_EXPIRY_HOURS = 72;
    private static final BigDecimal MAX_PAYMENT_AMOUNT = new BigDecimal("10000");
    private static final String PAYMENT_SERVICE = "paymentService";

    /**
     * Creates a payment request with enhanced validation and metrics
     */
    @Transactional
    @Bulkhead(name = PAYMENT_SERVICE)
    public PaymentRequestResponse createPaymentRequest(UUID requestorId, CreatePaymentRequestRequest request) {
        Timer.Sample timer = Timer.start(meterRegistry);
        log.info("Creating payment request from user {} to user {}", requestorId, request.getRecipientId());

        try {
            // Ensure recipient isn't the same as requestor
            if (requestorId.equals(request.getRecipientId())) {
                throw new InvalidPaymentOperationException("Cannot send payment request to yourself");
            }

            // Validate recipient exists
            UserResponse recipient = validateRecipientExists(request.getRecipientId());

            // Validate amount is within limits
            validatePaymentAmount(request.getAmount());

            // Create the payment request
            PaymentRequest paymentRequest = PaymentRequest.create(
                    requestorId,
                    request.getRecipientId(),
                    request.getAmount(),
                    request.getCurrency(),
                    request.getDescription(),
                    request.getExpiryHours() != null ? request.getExpiryHours() : DEFAULT_EXPIRY_HOURS
            );

            paymentRequest = paymentRequestRepository.save(paymentRequest);

            // Publish event for notification
            publishPaymentRequestEvent(paymentRequest, "CREATED");

            // Track successful payment request creation
            meterRegistry.counter("payment.requests.created").increment();

            // Get user information to include in response
            PaymentRequestResponse response = enrichWithUserInfo(mapToPaymentRequestResponse(paymentRequest));

            timer.stop(Timer.builder("payment.request.create.time")
                    .description("Time taken to create payment request")
                    .tags("status", "success")
                    .register(meterRegistry));

            return response;
        } catch (Exception e) {
            timer.stop(Timer.builder("payment.request.create.time")
                    .description("Time taken to create payment request")
                    .tags("status", "error")
                    .register(meterRegistry));

            meterRegistry.counter("payment.requests.errors", "type", e.getClass().getSimpleName()).increment();
            log.error("Error creating payment request", e);
            throw e;
        }
    }


    /**f
     * Enriches a payment request response with user information
     */
    private PaymentRequestResponse enrichWithUserInfo(PaymentRequestResponse response) {
        try {
            // Get requestor information
            UserResponse requestor = userClient.getUser(response.getRequestorId());
            if (requestor != null) {
                response.setRequestorName(requestor.getDisplayName());
            }

            // Get recipient information
            UserResponse recipient = userClient.getUser(response.getRecipientId());
            if (recipient != null) {
                response.setRecipientName(recipient.getDisplayName());
            }

            return response;
        } catch (Exception e) {
            log.warn("Could not enrich payment request with user info: {}", e.getMessage());
            return response; // Return the original response without enrichment
        }
    }

    /**
     * Validates that recipient exists
     */
    private UserResponse validateRecipientExists(UUID recipientId) {
        try {
            UserResponse recipient = userClient.getUser(recipientId);
            if (recipient == null) {
                throw new IllegalArgumentException("Recipient user not found: " + recipientId);
            }
            return recipient;
        } catch (Exception e) {
            log.error("Error validating recipient user", e);

            // More specific error based on the exception type
            if (e.getMessage() != null && e.getMessage().contains("404")) {
                throw new ResourceNotFoundException("Recipient user not found: " + recipientId);
            }

            throw new ServiceCommunicationException("Unable to validate recipient: " + e.getMessage(), e);
        }
    }

    /**
     * Validates payment amount is within limits
     */
    private void validatePaymentAmount(BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidPaymentOperationException("Amount must be greater than zero");
        }

        if (amount.compareTo(MAX_PAYMENT_AMOUNT) > 0) {
            throw new PaymentLimitExceededException("Amount exceeds maximum allowed: " + MAX_PAYMENT_AMOUNT);
        }
    }

    /**
     * Gets a payment request by ID with validation
     */
    @Transactional(readOnly = true)
    public PaymentRequestResponse getPaymentRequestById(UUID id) {
        log.info("Getting payment request with ID: {}", id);

        PaymentRequest paymentRequest = paymentRequestRepository.findById(id)
                .orElseThrow(() -> new PaymentRequestNotFoundException(id));

        return enrichWithUserInfo(mapToPaymentRequestResponse(paymentRequest));
    }

    /**
     * Approves a payment request with enhanced validation, error handling and transaction isolation
     */
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @CircuitBreaker(name = "walletService", fallbackMethod = "approvePaymentRequestFallback")
    @Retry(name = "walletService")
    @Bulkhead(name = PAYMENT_SERVICE)
    public PaymentRequestResponse approvePaymentRequest(UUID userId, UUID requestId, ApprovePaymentRequestRequest request) {
        Timer.Sample timer = Timer.start(meterRegistry);
        log.info("Approving payment request with ID: {}", requestId);

        try {
            PaymentRequest paymentRequest = paymentRequestRepository.findById(requestId)
                    .orElseThrow(() -> new PaymentRequestNotFoundException(requestId));

            // Verify user is the recipient of the request
            if (!paymentRequest.getRecipientId().equals(userId)) {
                throw new UnauthorizedAccessException("User is not the recipient of this payment request");
            }

            // Verify request is in PENDING status
            if (paymentRequest.getStatus() != PaymentRequestStatus.PENDING) {
                throw new InvalidPaymentStatusException(
                        "Payment request is not in PENDING status. Current status: " + paymentRequest.getStatus());
            }

            // Verify request is not expired
            if (paymentRequest.isExpired()) {
                paymentRequest.expire();
                paymentRequest = paymentRequestRepository.save(paymentRequest);
                throw new InvalidPaymentStatusException("Payment request has expired");
            }

            // Verify source wallet exists and belongs to the user
            WalletResponse wallet = validateWalletForPayment(userId, request.getSourceWalletId(),
                    paymentRequest.getAmount(), paymentRequest.getCurrency());

            // Execute the payment through the wallet service
            TransferRequest transferRequest = TransferRequest.builder()
                    .sourceWalletId(request.getSourceWalletId())
                    .targetWalletId(findOrGetDefaultWallet(paymentRequest.getRequestorId(), paymentRequest.getCurrency()))
                    .amount(paymentRequest.getAmount())
                    .description("Payment request: " + paymentRequest.getReferenceNumber())
                    .build();

            try {
                TransferResponse transferResponse = walletClient.transfer(transferRequest);

                // Update payment request status
                paymentRequest.approve(UUID.fromString(transferResponse.getId().toString()));
                paymentRequest = paymentRequestRepository.save(paymentRequest);

                // Publish event for notification
                publishPaymentRequestEvent(paymentRequest, "APPROVED");

                meterRegistry.counter("payment.requests.approved").increment();

                timer.stop(Timer.builder("payment.request.approve.time")
                        .description("Time taken to approve payment request")
                        .tags("status", "success")
                        .register(meterRegistry));

                return enrichWithUserInfo(mapToPaymentRequestResponse(paymentRequest));
            } catch (Exception e) {
                log.error("Failed to process payment request approval", e);

                // Publish event for notification with failure
                publishPaymentRequestFailureEvent(paymentRequest, "APPROVAL_FAILED", e.getMessage());

                meterRegistry.counter("payment.requests.approval.failed").increment();

                timer.stop(Timer.builder("payment.request.approve.time")
                        .description("Time taken to approve payment request")
                        .tags("status", "error")
                        .register(meterRegistry));

                // Throw appropriate exception based on error
                if (e.getMessage().contains("Insufficient")) {
                    throw new InsufficientFundsException("Insufficient funds in wallet: " + e.getMessage(), e);
                } else if (e.getMessage().contains("Wallet is not active")) {
                    throw new InvalidWalletStateException("Wallet is not in a valid state: " + e.getMessage(), e);
                } else {
                    throw new PaymentFailedException("Failed to process payment: " + e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            timer.stop(Timer.builder("payment.request.approve.time")
                    .description("Time taken to approve payment request")
                    .tags("status", "error", "exception", e.getClass().getSimpleName())
                    .register(meterRegistry));

            meterRegistry.counter("payment.requests.errors", "type", e.getClass().getSimpleName()).increment();

            log.error("Error approving payment request", e);
            throw e;
        }
    }

    /**
     * Validates wallet can be used for payment
     */
    private WalletResponse validateWalletForPayment(UUID userId, UUID walletId, BigDecimal amount, String currency) {
        try {
            WalletResponse wallet = walletClient.getWallet(walletId);

            if (wallet == null) {
                throw new ResourceNotFoundException("Wallet not found: " + walletId);
            }

            if (!wallet.getUserId().equals(userId)) {
                throw new UnauthorizedAccessException("Wallet does not belong to user");
            }

            // Verify wallet currency matches payment request currency
            if (!wallet.getCurrency().equals(currency)) {
                throw new InvalidPaymentOperationException(
                        "Wallet currency does not match payment request currency. " +
                                "Wallet: " + wallet.getCurrency() + ", Request: " + currency);
            }

            // Verify wallet has sufficient balance
            if (wallet.getBalance().compareTo(amount) < 0) {
                throw new InsufficientFundsException(
                        "Insufficient funds in wallet. Available: " + wallet.getBalance() +
                                " " + wallet.getCurrency() + ", Required: " + amount +
                                " " + currency);
            }

            // Verify wallet is active
            if (!"ACTIVE".equals(wallet.getStatus())) {
                throw new InvalidWalletStateException("Wallet is not active. Status: " + wallet.getStatus());
            }

            return wallet;
        } catch (Exception e) {
            log.error("Error validating wallet", e);

            if (e instanceof ResourceNotFoundException ||
                    e instanceof UnauthorizedAccessException ||
                    e instanceof InvalidPaymentOperationException ||
                    e instanceof InsufficientFundsException ||
                    e instanceof InvalidWalletStateException) {
                throw e;
            }

            throw new ServiceCommunicationException("Unable to validate wallet: " + e.getMessage(), e);
        }
    }

    /**
     * Fallback method for approving payment requests
     */
    private PaymentRequestResponse approvePaymentRequestFallback(UUID userId, UUID requestId,
                                                                 ApprovePaymentRequestRequest request, Exception e) {
        log.warn("Fallback for approvePaymentRequest executed: {}", e.getMessage());
        meterRegistry.counter("payment.requests.fallbacks").increment();
        throw new ServiceUnavailableException("Payment service temporarily unavailable. Please try again later.", e);
    }

    /**
     * Find or get default wallet for a user in a specific currency with enhanced error handling
     */
    private UUID findOrGetDefaultWallet(UUID userId, String currency) {
        try {
            List<WalletResponse> wallets = walletClient.getUserWallets(userId);

            if (wallets.isEmpty()) {
                throw new ResourceNotFoundException("No wallets found for user " + userId);
            }

            // First try to find a wallet with the matching currency
            Optional<WalletResponse> matchingWallet = wallets.stream()
                    .filter(w -> currency.equals(w.getCurrency()) && "ACTIVE".equals(w.getStatus()))
                    .findFirst();

            if (matchingWallet.isPresent()) {
                return matchingWallet.get().getId();
            }

            // If no matching currency wallet, get the first active wallet
            Optional<WalletResponse> anyActiveWallet = wallets.stream()
                    .filter(w -> "ACTIVE".equals(w.getStatus()))
                    .findFirst();

            if (anyActiveWallet.isPresent()) {
                log.warn("No wallet found with currency {}, using default wallet with currency {}",
                        currency, anyActiveWallet.get().getCurrency());
                return anyActiveWallet.get().getId();
            }

            throw new ResourceNotFoundException("No active wallet found for user " + userId);
        } catch (Exception e) {
            log.error("Error finding default wallet", e);

            if (e instanceof ResourceNotFoundException) {
                throw e;
            }

            throw new ServiceCommunicationException("Unable to find a wallet for the recipient: " + e.getMessage(), e);
        }
    }

    /**
     * Enhanced scheduled task to expire payment requests with error handling
     */
    @Scheduled(cron = "0 0 * * * *") // Every hour
    @Transactional
    public void expirePaymentRequests() {
        Timer.Sample timer = Timer.start(meterRegistry);
        log.info("Checking for expired payment requests");

        try {
            List<PaymentRequest> expiredRequests = paymentRequestRepository.findByStatusAndExpiryDateBefore(
                    PaymentRequestStatus.PENDING, LocalDateTime.now());

            int count = 0;
            for (PaymentRequest paymentRequest : expiredRequests) {
                try {
                    paymentRequest.expire();
                    paymentRequestRepository.save(paymentRequest);

                    // Publish event for notification
                    publishPaymentRequestEvent(paymentRequest, "EXPIRED");

                    log.info("Marked payment request as expired: {}", paymentRequest.getId());
                    count++;
                } catch (Exception e) {
                    log.error("Error expiring payment request {}", paymentRequest.getId(), e);
                    meterRegistry.counter("payment.requests.expire.errors").increment();
                    // Continue with other requests
                }
            }

            log.info("Expired {} payment requests", count);
            meterRegistry.counter("payment.requests.expired").increment(count);

            timer.stop(Timer.builder("payment.requests.expire.time")
                    .description("Time taken to expire payment requests")
                    .register(meterRegistry));
        } catch (Exception e) {
            log.error("Error in scheduled payment request expiration", e);
            meterRegistry.counter("payment.scheduled.tasks.errors",
                    "task", "expirePaymentRequests").increment();

            timer.stop(Timer.builder("payment.requests.expire.time")
                    .description("Time taken to expire payment requests")
                    .tags("status", "error")
                    .register(meterRegistry));
        }
    }

    // Other existing methods...

    /**
     * Publishes a payment request event to Kafka
     */
    private void publishPaymentRequestEvent(PaymentRequest paymentRequest, String eventType) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("id", paymentRequest.getId().toString());
            event.put("type", eventType);
            event.put("requestorId", paymentRequest.getRequestorId().toString());
            event.put("recipientId", paymentRequest.getRecipientId().toString());
            event.put("amount", paymentRequest.getAmount());
            event.put("currency", paymentRequest.getCurrency());
            event.put("status", paymentRequest.getStatus().toString());
            event.put("timestamp", LocalDateTime.now().toString());

            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, paymentRequest.getId().toString(), payload);

            log.info("Published payment request event: {}, type: {}", paymentRequest.getId(), eventType);
        } catch (Exception e) {
            log.error("Failed to publish payment request event", e);
            meterRegistry.counter("payment.events.publish.errors").increment();
        }
    }

    /**
     * Publishes a payment request failure event to Kafka
     */
    private void publishPaymentRequestFailureEvent(PaymentRequest paymentRequest, String eventType, String reason) {
        try {
            Map<String, Object> event = new HashMap<>();
            event.put("id", paymentRequest.getId().toString());
            event.put("type", eventType);
            event.put("requestorId", paymentRequest.getRequestorId().toString());
            event.put("recipientId", paymentRequest.getRecipientId().toString());
            event.put("amount", paymentRequest.getAmount());
            event.put("currency", paymentRequest.getCurrency());
            event.put("status", paymentRequest.getStatus().toString());
            event.put("reason", reason);
            event.put("timestamp", LocalDateTime.now().toString());

            String payload = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(PAYMENT_EVENTS_TOPIC, paymentRequest.getId().toString(), payload);

            log.info("Published payment request failure event: {}, type: {}, reason: {}",
                    paymentRequest.getId(), eventType, reason);
        } catch (Exception e) {
            log.error("Failed to publish payment request failure event", e);
            meterRegistry.counter("payment.events.publish.errors").increment();
        }
    }


    /**
     * Maps a PaymentRequest entity to a PaymentRequestResponse DTO
     */
    private PaymentRequestResponse mapToPaymentRequestResponse(PaymentRequest paymentRequest) {
        return PaymentRequestResponse.builder()
                .id(paymentRequest.getId())
                .requestorId(paymentRequest.getRequestorId())
                .recipientId(paymentRequest.getRecipientId())
                .amount(paymentRequest.getAmount())
                .currency(paymentRequest.getCurrency())
                .description(paymentRequest.getDescription())
                .referenceNumber(paymentRequest.getReferenceNumber())
                .status(paymentRequest.getStatus().toString())
                .transactionId(paymentRequest.getTransactionId())
                .expiryDate(paymentRequest.getExpiryDate())
                .createdAt(paymentRequest.getCreatedAt())
                .updatedAt(paymentRequest.getUpdatedAt())
                .build();
    }

    /**
     * Custom exceptions for more specific error handling
     */
    public static class UnauthorizedAccessException extends RuntimeException {
        public UnauthorizedAccessException(String message) {
            super(message);
        }
    }

    public static class InvalidWalletStateException extends RuntimeException {
        public InvalidWalletStateException(String message) {
            super(message);
        }

        public InvalidWalletStateException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ServiceCommunicationException extends RuntimeException {
        public ServiceCommunicationException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ServiceUnavailableException extends RuntimeException {
        public ServiceUnavailableException(String message, Throwable cause) {
            super(message, cause);
        }
    }

    public static class ResourceNotFoundException extends RuntimeException {
        public ResourceNotFoundException(String message) {
            super(message);
        }
    }

    public static class InsufficientFundsException extends RuntimeException {
        public InsufficientFundsException(String message, Throwable cause) {
            super(message, cause);
        }

        public InsufficientFundsException(String message) {
            super(message);
        }
    }
}