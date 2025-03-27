/**
 * File: src/test/java/com/p2pfinance/notification/messaging/NotificationEventListenerTest.java
 */
package com.p2pfinance.notification.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p2pfinance.notification.dto.SendNotificationRequest;
import com.p2pfinance.notification.event.*;
import com.p2pfinance.notification.service.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
class NotificationEventListenerTest {

    @Mock
    private NotificationService notificationService;

    @Mock
    private ObjectMapper objectMapper;

    @Captor
    private ArgumentCaptor<SendNotificationRequest> requestCaptor;

    private NotificationEventListener eventListener;

    @BeforeEach
    void setUp() {
        eventListener = new NotificationEventListener(notificationService, objectMapper);
    }

    @Test
    void consumeUserEvents_ShouldHandleUserRegisteredEvent() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        String username = "john.doe";
        String email = "john.doe@example.com";

        String eventJson = String.format(
                "{\"eventType\":\"USER_REGISTERED\",\"userId\":\"%s\",\"username\":\"%s\",\"email\":\"%s\"}",
                userId, username, email);

        // Mock both the initial type detection and the specific event deserialization
        UserRegisteredEvent event = new UserRegisteredEvent();
        event.setUserId(userId);
        event.setUsername(username);
        event.setEmail(email);
        event.setEventType("USER_REGISTERED");

        // First call for determining event type
        when(objectMapper.readValue(eq(eventJson), eq(NotificationEvent.class)))
                .thenReturn(event);

        // Second call for actual event data
        when(objectMapper.readValue(eq(eventJson), eq(UserRegisteredEvent.class)))
                .thenReturn(event);

        // When
        eventListener.consumeUserEvents(eventJson);

        // Then
        verify(notificationService).sendNotification(requestCaptor.capture());
        SendNotificationRequest request = requestCaptor.getValue();

        assertThat(request.getUserId()).isEqualTo(userId);
        assertThat(request.getTemplateCode()).isEqualTo("user_registered");
        assertThat(request.getParameters()).containsEntry("username", username);
    }

    @Test
    void consumeWalletEvents_ShouldHandleWalletTransactionEvent() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        UUID walletId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        String transactionType = "DEPOSIT";
        BigDecimal amount = new BigDecimal("100.00");
        String currency = "USD";
        BigDecimal newBalance = new BigDecimal("500.00");

        String eventJson = String.format(
                "{\"eventType\":\"WALLET_TRANSACTION\",\"userId\":\"%s\",\"walletId\":\"%s\"," +
                        "\"transactionId\":\"%s\",\"transactionType\":\"%s\",\"amount\":%s," +
                        "\"currency\":\"%s\",\"newBalance\":%s}",
                userId, walletId, transactionId, transactionType, amount, currency, newBalance);

        // Create base event for type detection
        NotificationEvent baseEvent = mock(NotificationEvent.class);
        when(baseEvent.getEventType()).thenReturn("WALLET_TRANSACTION");

        // Create wallet event for handler
        WalletTransactionEvent walletEvent = WalletTransactionEvent.builder()
                .userId(userId)
                .walletId(walletId)
                .transactionId(transactionId)
                .transactionType(transactionType)
                .amount(amount)
                .currency(currency)
                .newBalance(newBalance)
                .build();
        walletEvent.setEventType("WALLET_TRANSACTION");

        // Mock both calls to ObjectMapper
        when(objectMapper.readValue(eq(eventJson), eq(NotificationEvent.class)))
                .thenReturn(baseEvent);
        when(objectMapper.readValue(eq(eventJson), eq(WalletTransactionEvent.class)))
                .thenReturn(walletEvent);

        // When
        eventListener.consumeWalletEvents(eventJson);

        // Then
        verify(notificationService).sendNotification(requestCaptor.capture());
        SendNotificationRequest request = requestCaptor.getValue();

        assertThat(request.getUserId()).isEqualTo(userId);
        assertThat(request.getTemplateCode()).isEqualTo("wallet_deposit");
        assertThat(request.getParameters()).containsEntry("amount", amount);
        assertThat(request.getParameters()).containsEntry("currency", currency);
        assertThat(request.getParameters()).containsEntry("newBalance", newBalance);
        assertThat(request.getReferenceId()).isEqualTo(transactionId.toString());
    }

    @Test
    void consumePaymentRequestEvents_ShouldHandlePaymentRequestEvent() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        UUID requestId = UUID.randomUUID();
        String status = "APPROVED";
        UUID requestorId = UUID.randomUUID();
        String requestorName = "Jane Doe";
        BigDecimal amount = new BigDecimal("50.00");
        String currency = "EUR";

        String eventJson = String.format(
                "{\"eventType\":\"PAYMENT_REQUEST\",\"userId\":\"%s\",\"requestId\":\"%s\"," +
                        "\"status\":\"%s\",\"requestorId\":\"%s\",\"requestorName\":\"%s\"," +
                        "\"amount\":%s,\"currency\":\"%s\"}",
                userId, requestId, status, requestorId, requestorName, amount, currency);

        // Create base event for type detection
        NotificationEvent baseEvent = mock(NotificationEvent.class);
        when(baseEvent.getEventType()).thenReturn("PAYMENT_REQUEST");

        // Create payment request event
        PaymentRequestEvent paymentEvent = PaymentRequestEvent.builder()
                .userId(userId)
                .requestId(requestId)
                .status(status)
                .requestorId(requestorId)
                .requestorName(requestorName)
                .amount(amount)
                .currency(currency)
                .build();
        paymentEvent.setEventType("PAYMENT_REQUEST");

        // Mock the ObjectMapper
        when(objectMapper.readValue(eq(eventJson), eq(NotificationEvent.class)))
                .thenReturn(baseEvent);
        when(objectMapper.readValue(eq(eventJson), eq(PaymentRequestEvent.class)))
                .thenReturn(paymentEvent);

        // When
        eventListener.consumePaymentRequestEvents(eventJson);

        // Then
        verify(notificationService).sendNotification(requestCaptor.capture());
        SendNotificationRequest request = requestCaptor.getValue();

        assertThat(request.getUserId()).isEqualTo(userId);
        assertThat(request.getTemplateCode()).isEqualTo("payment_request_approved");
        assertThat(request.getParameters()).containsEntry("amount", amount);
        assertThat(request.getParameters()).containsEntry("currency", currency);
        assertThat(request.getParameters()).containsEntry("requestorName", requestorName);
        assertThat(request.getReferenceId()).isEqualTo(requestId.toString());
    }

    @Test
    void consumeScheduledPaymentEvents_ShouldHandleScheduledPaymentEvent() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        String status = "EXECUTED";
        BigDecimal amount = new BigDecimal("75.00");
        String currency = "GBP";
        LocalDateTime executionDate = LocalDateTime.parse("2025-03-20T10:15:30");

        String eventJson = String.format(
                "{\"eventType\":\"SCHEDULED_PAYMENT\",\"userId\":\"%s\",\"paymentId\":\"%s\"," +
                        "\"status\":\"%s\",\"amount\":%s,\"currency\":\"%s\",\"executionDate\":\"%s\"}",
                userId, paymentId, status, amount, currency, "2025-03-20T10:15:30");

        // Create base event for type detection
        NotificationEvent baseEvent = mock(NotificationEvent.class);
        when(baseEvent.getEventType()).thenReturn("SCHEDULED_PAYMENT");

        // Create scheduled payment event
        ScheduledPaymentEvent scheduledEvent = ScheduledPaymentEvent.builder()
                .userId(userId)
                .paymentId(paymentId)
                .status(status)
                .amount(amount)
                .currency(currency)
                .executionDate(executionDate)
                .build();
        scheduledEvent.setEventType("SCHEDULED_PAYMENT");

        // Mock the ObjectMapper
        when(objectMapper.readValue(eq(eventJson), eq(NotificationEvent.class)))
                .thenReturn(baseEvent);
        when(objectMapper.readValue(eq(eventJson), eq(ScheduledPaymentEvent.class)))
                .thenReturn(scheduledEvent);

        // When
        eventListener.consumeScheduledPaymentEvents(eventJson);

        // Then
        verify(notificationService).sendNotification(requestCaptor.capture());
        SendNotificationRequest request = requestCaptor.getValue();

        assertThat(request.getUserId()).isEqualTo(userId);
        assertThat(request.getTemplateCode()).isEqualTo("scheduled_payment_executed");
        assertThat(request.getParameters()).containsEntry("amount", amount);
        assertThat(request.getParameters()).containsEntry("currency", currency);
        assertThat(request.getParameters()).containsEntry("executionDate", executionDate);
        assertThat(request.getReferenceId()).isEqualTo(paymentId.toString());
    }

    @Test
    void consumeSplitPaymentEvents_ShouldHandleSplitPaymentEvent() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        UUID paymentId = UUID.randomUUID();
        String status = "COMPLETED";
        String title = "Dinner bill";
        BigDecimal totalAmount = new BigDecimal("120.00");
        String currency = "USD";

        String eventJson = String.format(
                "{\"eventType\":\"SPLIT_PAYMENT\",\"userId\":\"%s\",\"paymentId\":\"%s\"," +
                        "\"status\":\"%s\",\"title\":\"%s\",\"totalAmount\":%s,\"currency\":\"%s\"}",
                userId, paymentId, status, title, totalAmount, currency);

        // Create base event for type detection
        NotificationEvent baseEvent = mock(NotificationEvent.class);
        when(baseEvent.getEventType()).thenReturn("SPLIT_PAYMENT");

        // Create split payment event
        SplitPaymentEvent splitEvent = SplitPaymentEvent.builder()
                .userId(userId)
                .paymentId(paymentId)
                .status(status)
                .title(title)
                .totalAmount(totalAmount)
                .currency(currency)
                .build();
        splitEvent.setEventType("SPLIT_PAYMENT");

        // Mock the ObjectMapper
        when(objectMapper.readValue(eq(eventJson), eq(NotificationEvent.class)))
                .thenReturn(baseEvent);
        when(objectMapper.readValue(eq(eventJson), eq(SplitPaymentEvent.class)))
                .thenReturn(splitEvent);

        // When
        eventListener.consumeSplitPaymentEvents(eventJson);

        // Then
        verify(notificationService).sendNotification(requestCaptor.capture());
        SendNotificationRequest request = requestCaptor.getValue();

        assertThat(request.getUserId()).isEqualTo(userId);
        assertThat(request.getTemplateCode()).isEqualTo("split_payment_completed");
        assertThat(request.getParameters()).containsEntry("title", title);
        assertThat(request.getParameters()).containsEntry("totalAmount", totalAmount);
        assertThat(request.getParameters()).containsEntry("currency", currency);
        assertThat(request.getReferenceId()).isEqualTo(paymentId.toString());
    }

    @Test
    void consumeSecurityEvents_ShouldHandleSecurityEvent() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        String securityEventType = "LOGIN";
        String ipAddress = "192.168.1.1";
        String deviceInfo = "Chrome on Windows";
        LocalDateTime eventTime = LocalDateTime.parse("2025-03-27T15:30:45");
        boolean suspicious = true;

        String eventJson = String.format(
                "{\"eventType\":\"SECURITY\",\"userId\":\"%s\",\"securityEventType\":\"%s\"," +
                        "\"ipAddress\":\"%s\",\"deviceInfo\":\"%s\",\"eventTime\":\"%s\",\"suspicious\":%s}",
                userId, securityEventType, ipAddress, deviceInfo, "2025-03-27T15:30:45", suspicious);

        // Create base event for type detection
        NotificationEvent baseEvent = mock(NotificationEvent.class);
        when(baseEvent.getEventType()).thenReturn("SECURITY");

        // Create security event
        SecurityEvent securityEvent = SecurityEvent.builder()
                .userId(userId)
                .securityEventType(securityEventType)
                .ipAddress(ipAddress)
                .deviceInfo(deviceInfo)
                .eventTime(eventTime)
                .suspicious(suspicious)
                .build();
        securityEvent.setEventType("SECURITY");

        // Mock the ObjectMapper
        when(objectMapper.readValue(eq(eventJson), eq(NotificationEvent.class)))
                .thenReturn(baseEvent);
        when(objectMapper.readValue(eq(eventJson), eq(SecurityEvent.class)))
                .thenReturn(securityEvent);

        // When
        eventListener.consumeSecurityEvents(eventJson);

        // Then
        verify(notificationService).sendNotification(requestCaptor.capture());
        SendNotificationRequest request = requestCaptor.getValue();

        assertThat(request.getUserId()).isEqualTo(userId);
        assertThat(request.getTemplateCode()).isEqualTo("security_login");
        assertThat(request.getParameters()).containsEntry("ipAddress", ipAddress);
        assertThat(request.getParameters()).containsEntry("deviceInfo", deviceInfo);
        // For suspicious activities, both APP and EMAIL channels should be used
        assertThat(request.getTypes()).contains("APP", "EMAIL");
    }

    @Test
    void malformedEvent_ShouldLogErrorAndNotThrowException() throws Exception {
        // Given
        String malformedJson = "{\"eventType\":\"USER_REGISTERED\", \"userId\":\"invalid-uuid\", bad json}";

        // Mock error during JSON parsing
        when(objectMapper.readValue(eq(malformedJson), any(Class.class)))
                .thenThrow(new RuntimeException("Invalid JSON"));

        // When & Then - should not throw exception
        eventListener.consumeUserEvents(malformedJson);

        // Verify no notification was sent
        verify(notificationService, never()).sendNotification(any());
    }

    @Test
    void unknownEventType_ShouldLogWarningAndTakeNoAction() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        String unknownEventJson = String.format(
                "{\"eventType\":\"UNKNOWN_EVENT\",\"userId\":\"%s\"}", userId);

        // Create mock event with unknown type
        NotificationEvent unknownEvent = mock(NotificationEvent.class);
        when(unknownEvent.getEventType()).thenReturn("UNKNOWN_EVENT");

        // Mock ObjectMapper
        when(objectMapper.readValue(eq(unknownEventJson), any(Class.class)))
                .thenReturn(unknownEvent);

        // When
        eventListener.consumeUserEvents(unknownEventJson);

        // Then
        verify(notificationService, never()).sendNotification(any());
    }
}