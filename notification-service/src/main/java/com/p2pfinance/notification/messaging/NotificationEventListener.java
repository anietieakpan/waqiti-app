/**
 * File: src/main/java/com/p2pfinance/notification/messaging/NotificationEventListener.java
 */
package com.p2pfinance.notification.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p2pfinance.notification.dto.SendNotificationRequest;
import com.p2pfinance.notification.event.*;
import com.p2pfinance.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationEventListener {
    private final NotificationService notificationService;
    private final ObjectMapper objectMapper;

    // User event listeners

    @KafkaListener(topics = "user-events", groupId = "notification-service")
    public void consumeUserEvents(String message) {
        try {
            // Determine the event type
            NotificationEvent baseEvent = objectMapper.readValue(message, NotificationEvent.class);

            switch (baseEvent.getEventType()) {
                case "USER_REGISTERED" -> handleUserRegisteredEvent(message);
                case "USER_VERIFIED" -> handleUserVerifiedEvent(message);
                default -> log.warn("Unknown user event type: {}", baseEvent.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing user event", e);
        }
    }

    private void handleUserRegisteredEvent(String message) throws Exception {
        UserRegisteredEvent event = objectMapper.readValue(message, UserRegisteredEvent.class);
        // Initialize the event type since we removed the custom constructor
        event.initializeEventType();

        log.info("Received user registered event for user: {}", event.getUserId());

        Map<String, Object> params = new HashMap<>();
        params.put("username", event.getUsername());

        SendNotificationRequest request = SendNotificationRequest.builder()
                .userId(event.getUserId())
                .templateCode("user_registered")
                .parameters(params)
                .types(new String[] { "EMAIL" }) // Force email notification for registration
                .build();

        notificationService.sendNotification(request);
    }

    private void handleUserVerifiedEvent(String message) throws Exception {
        UserVerifiedEvent event = objectMapper.readValue(message, UserVerifiedEvent.class);
        // Initialize the event type since we removed the custom constructor
        event.initializeEventType();

        log.info("Received user verified event for user: {}", event.getUserId());

        Map<String, Object> params = new HashMap<>();
        params.put("username", event.getUsername());

        SendNotificationRequest request = SendNotificationRequest.builder()
                .userId(event.getUserId())
                .templateCode("user_verified")
                .parameters(params)
                .build();

        notificationService.sendNotification(request);
    }

    // Wallet event listeners

    @KafkaListener(topics = "wallet-events", groupId = "notification-service")
    public void consumeWalletEvents(String message) {
        try {
            // Determine the event type
            NotificationEvent baseEvent = objectMapper.readValue(message, NotificationEvent.class);

            if ("WALLET_TRANSACTION".equals(baseEvent.getEventType())) {
                handleWalletTransactionEvent(message);
            } else {
                log.warn("Unknown wallet event type: {}", baseEvent.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing wallet event", e);
        }
    }

    private void handleWalletTransactionEvent(String message) throws Exception {
        WalletTransactionEvent event = objectMapper.readValue(message, WalletTransactionEvent.class);
        // Initialize event type if needed (assuming WalletTransactionEvent has a
        // similar structure)
        if (event.getEventType() == null) {
            event.setEventType("WALLET_TRANSACTION");
        }

        log.info("Received wallet transaction event for user: {}, type: {}",
                event.getUserId(), event.getTransactionType());

        String templateCode;
        switch (event.getTransactionType()) {
            case "DEPOSIT" -> templateCode = "wallet_deposit";
            case "WITHDRAWAL" -> templateCode = "wallet_withdrawal";
            case "TRANSFER" -> templateCode = "wallet_transfer";
            default -> {
                log.warn("Unknown transaction type: {}", event.getTransactionType());
                return;
            }
        }

        Map<String, Object> params = new HashMap<>();
        params.put("amount", event.getAmount());
        params.put("currency", event.getCurrency());
        params.put("newBalance", event.getNewBalance());

        if (event.getCounterpartyName() != null) {
            params.put("counterpartyName", event.getCounterpartyName());
        }

        SendNotificationRequest request = SendNotificationRequest.builder()
                .userId(event.getUserId())
                .templateCode(templateCode)
                .parameters(params)
                .referenceId(event.getTransactionId().toString())
                .build();

        notificationService.sendNotification(request);
    }

    // Payment request event listeners

    @KafkaListener(topics = "payment-request-events", groupId = "notification-service")
    public void consumePaymentRequestEvents(String message) {
        try {
            // Determine the event type
            NotificationEvent baseEvent = objectMapper.readValue(message, NotificationEvent.class);

            if ("PAYMENT_REQUEST".equals(baseEvent.getEventType())) {
                handlePaymentRequestEvent(message);
            } else {
                log.warn("Unknown payment request event type: {}", baseEvent.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing payment request event", e);
        }
    }

    private void handlePaymentRequestEvent(String message) throws Exception {
        PaymentRequestEvent event = objectMapper.readValue(message, PaymentRequestEvent.class);
        // Initialize event type if needed
        if (event.getEventType() == null) {
            event.setEventType("PAYMENT_REQUEST");
        }

        log.info("Received payment request event for user: {}, status: {}",
                event.getUserId(), event.getStatus());

        String templateCode;
        switch (event.getStatus()) {
            case "CREATED" -> templateCode = "payment_request_created";
            case "APPROVED" -> templateCode = "payment_request_approved";
            case "REJECTED" -> templateCode = "payment_request_rejected";
            case "CANCELED" -> templateCode = "payment_request_canceled";
            case "EXPIRED" -> templateCode = "payment_request_expired";
            default -> {
                log.warn("Unknown payment request status: {}", event.getStatus());
                return;
            }
        }

        Map<String, Object> params = new HashMap<>();
        params.put("amount", event.getAmount());
        params.put("currency", event.getCurrency());

        if (event.getRequestorName() != null) {
            params.put("requestorName", event.getRequestorName());
        }

        if (event.getRecipientName() != null) {
            params.put("recipientName", event.getRecipientName());
        }

        SendNotificationRequest request = SendNotificationRequest.builder()
                .userId(event.getUserId())
                .templateCode(templateCode)
                .parameters(params)
                .referenceId(event.getRequestId().toString())
                .build();

        notificationService.sendNotification(request);
    }

    // Scheduled payment event listeners

    @KafkaListener(topics = "scheduled-payment-events", groupId = "notification-service")
    public void consumeScheduledPaymentEvents(String message) {
        try {
            // Determine the event type
            NotificationEvent baseEvent = objectMapper.readValue(message, NotificationEvent.class);

            if ("SCHEDULED_PAYMENT".equals(baseEvent.getEventType())) {
                handleScheduledPaymentEvent(message);
            } else {
                log.warn("Unknown scheduled payment event type: {}", baseEvent.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing scheduled payment event", e);
        }
    }

    private void handleScheduledPaymentEvent(String message) throws Exception {
        ScheduledPaymentEvent event = objectMapper.readValue(message, ScheduledPaymentEvent.class);
        // Initialize event type if needed
        if (event.getEventType() == null) {
            event.setEventType("SCHEDULED_PAYMENT");
        }

        log.info("Received scheduled payment event for user: {}, status: {}",
                event.getUserId(), event.getStatus());

        String templateCode;
        switch (event.getStatus()) {
            case "CREATED" -> templateCode = "scheduled_payment_created";
            case "EXECUTED" -> templateCode = "scheduled_payment_executed";
            case "FAILED" -> templateCode = "scheduled_payment_failed";
            case "COMPLETED" -> templateCode = "scheduled_payment_completed";
            case "CANCELED" -> templateCode = "scheduled_payment_canceled";
            default -> {
                log.warn("Unknown scheduled payment status: {}", event.getStatus());
                return;
            }
        }

        Map<String, Object> params = new HashMap<>();
        params.put("amount", event.getAmount());
        params.put("currency", event.getCurrency());

        if (event.getExecutionDate() != null) {
            params.put("executionDate", event.getExecutionDate());
        }

        if (event.getSenderName() != null) {
            params.put("senderName", event.getSenderName());
        }

        if (event.getRecipientName() != null) {
            params.put("recipientName", event.getRecipientName());
        }

        SendNotificationRequest request = SendNotificationRequest.builder()
                .userId(event.getUserId())
                .templateCode(templateCode)
                .parameters(params)
                .referenceId(event.getPaymentId().toString())
                .build();

        notificationService.sendNotification(request);
    }

    // Split payment event listeners

    @KafkaListener(topics = "split-payment-events", groupId = "notification-service")
    public void consumeSplitPaymentEvents(String message) {
        try {
            // Determine the event type
            NotificationEvent baseEvent = objectMapper.readValue(message, NotificationEvent.class);

            if ("SPLIT_PAYMENT".equals(baseEvent.getEventType())) {
                handleSplitPaymentEvent(message);
            } else {
                log.warn("Unknown split payment event type: {}", baseEvent.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing split payment event", e);
        }
    }

    private void handleSplitPaymentEvent(String message) throws Exception {
        SplitPaymentEvent event = objectMapper.readValue(message, SplitPaymentEvent.class);
        // Initialize event type if needed
        if (event.getEventType() == null) {
            event.setEventType("SPLIT_PAYMENT");
        }

        log.info("Received split payment event for user: {}, status: {}",
                event.getUserId(), event.getStatus());

        String templateCode;
        switch (event.getStatus()) {
            case "CREATED" -> templateCode = "split_payment_created";
            case "PARTICIPANT_ADDED" -> templateCode = "split_payment_participant_added";
            case "PARTICIPANT_PAID" -> templateCode = "split_payment_participant_paid";
            case "COMPLETED" -> templateCode = "split_payment_completed";
            case "CANCELED" -> templateCode = "split_payment_canceled";
            default -> {
                log.warn("Unknown split payment status: {}", event.getStatus());
                return;
            }
        }

        Map<String, Object> params = new HashMap<>();
        params.put("title", event.getTitle());
        params.put("totalAmount", event.getTotalAmount());
        params.put("currency", event.getCurrency());

        if (event.getUserAmount() != null) {
            params.put("userAmount", event.getUserAmount());
        }

        if (event.getOrganizerName() != null) {
            params.put("organizerName", event.getOrganizerName());
        }

        if (event.getParticipantName() != null) {
            params.put("participantName", event.getParticipantName());
        }

        SendNotificationRequest request = SendNotificationRequest.builder()
                .userId(event.getUserId())
                .templateCode(templateCode)
                .parameters(params)
                .referenceId(event.getPaymentId().toString())
                .build();

        notificationService.sendNotification(request);
    }

    // Security event listeners

    @KafkaListener(topics = "security-events", groupId = "notification-service")
    public void consumeSecurityEvents(String message) {
        try {
            // Determine the event type
            NotificationEvent baseEvent = objectMapper.readValue(message, NotificationEvent.class);

            if ("SECURITY".equals(baseEvent.getEventType())) {
                handleSecurityEvent(message);
            } else {
                log.warn("Unknown security event type: {}", baseEvent.getEventType());
            }
        } catch (Exception e) {
            log.error("Error processing security event", e);
        }
    }

    private void handleSecurityEvent(String message) throws Exception {
        SecurityEvent event = objectMapper.readValue(message, SecurityEvent.class);
        // Initialize event type if needed
        if (event.getEventType() == null) {
            event.setEventType("SECURITY");
        }

        log.info("Received security event for user: {}, type: {}",
                event.getUserId(), event.getSecurityEventType());

        String templateCode;
        switch (event.getSecurityEventType()) {
            case "LOGIN" -> templateCode = "security_login";
            case "PASSWORD_CHANGED" -> templateCode = "security_password_changed";
            case "DEVICE_ADDED" -> templateCode = "security_device_added";
            case "SUSPICIOUS_ACTIVITY" -> templateCode = "security_suspicious_activity";
            default -> {
                log.warn("Unknown security event type: {}", event.getSecurityEventType());
                return;
            }
        }

        Map<String, Object> params = new HashMap<>();
        params.put("eventTime", event.getEventTime());

        if (event.getIpAddress() != null) {
            params.put("ipAddress", event.getIpAddress());
        }

        if (event.getDeviceInfo() != null) {
            params.put("deviceInfo", event.getDeviceInfo());
        }

        if (event.getLocation() != null) {
            params.put("location", event.getLocation());
        }

        // For suspicious activities, always send via email too
        String[] types = event.isSuspicious() ? new String[] { "APP", "EMAIL" } : null;

        SendNotificationRequest request = SendNotificationRequest.builder()
                .userId(event.getUserId())
                .templateCode(templateCode)
                .parameters(params)
                .types(types)
                .build();

        notificationService.sendNotification(request);
    }
}