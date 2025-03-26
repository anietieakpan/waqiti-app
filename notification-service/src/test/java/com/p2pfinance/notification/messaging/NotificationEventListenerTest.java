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
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
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

        UserRegisteredEvent event = new UserRegisteredEvent(userId, username, email);
        String eventJson = "{\"eventType\":\"USER_REGISTERED\",\"userId\":\"" + userId + "\"," +
                "\"username\":\"" + username + "\",\"email\":\"" + email + "\"}";

        when(objectMapper.readValue(eq(eventJson), any(Class.class)))
                .thenReturn(event);
        when(notificationService.sendNotification(any()))
                .thenReturn(null);

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

        WalletTransactionEvent event = WalletTransactionEvent.builder()
                .userId(userId)
                .walletId(walletId)
                .transactionId(transactionId)
                .transactionType(transactionType)
                .amount(amount)
                .currency(currency)
                .newBalance(newBalance)
                .build();

        String eventJson = "{\"eventType\":\"WALLET_TRANSACTION\",\"userId\":\"" + userId + "\"," +
                "\"transactionType\":\"" + transactionType + "\"}";

        when(objectMapper.readValue(eq(eventJson), any(Class.class)))
                .thenReturn(event);
        when(notificationService.sendNotification(any()))
                .thenReturn(null);

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

        PaymentRequestEvent event = PaymentRequestEvent.builder()
                .userId(userId)
                .requestId(requestId)
                .status(status)
                .requestorId(requestorId)
                .requestorName(requestorName)
                .amount(amount)
                .currency(currency)
                .build();

        String eventJson = "{\"eventType\":\"PAYMENT_REQUEST\",\"userId\":\"" + userId + "\"," +
                "\"status\":\"" + status + "\"}";

        when(objectMapper.readValue(eq(eventJson), any(Class.class)))
                .thenReturn(event);
        when(notificationService.sendNotification(any()))
                .thenReturn(null);

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
        LocalDateTime executionDate = LocalDateTime.now();

        ScheduledPaymentEvent event = ScheduledPaymentEvent.builder()
                .userId(userId)
                .paymentId(paymentId)
                .status(status)
                .amount(amount)
                .currency(currency)
                .executionDate(executionDate)
                .build();

        String eventJson = "{\"eventType\":\"SCHEDULED_PAYMENT\",\"userId\":\"" + userId + "\"," +
                "\"status\":\"" + status + "\"}";

        when(objectMapper.readValue(eq(eventJson), any(Class.class)))
                .thenReturn(event);
        when(notificationService.sendNotification(any()))
                .thenReturn(null);

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

        SplitPaymentEvent event = SplitPaymentEvent.builder()
                .userId(userId)
                .paymentId(paymentId)
                .status(status)
                .title(title)
                .totalAmount(totalAmount)
                .currency(currency)
                .build();

        String eventJson = "{\"eventType\":\"SPLIT_PAYMENT\",\"userId\":\"" + userId + "\"," +
                "\"status\":\"" + status + "\"}";

        when(objectMapper.readValue(eq(eventJson), any(Class.class)))
                .thenReturn(event);
        when(notificationService.sendNotification(any()))
                .thenReturn(null);

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
        LocalDateTime eventTime = LocalDateTime.now();
        boolean suspicious = true;

        SecurityEvent event = SecurityEvent.builder()
                .userId(userId)
                .securityEventType(securityEventType)
                .ipAddress(ipAddress)
                .deviceInfo(deviceInfo)
                .eventTime(eventTime)
                .suspicious(suspicious)
                .build();

        String eventJson = "{\"eventType\":\"SECURITY\",\"userId\":\"" + userId + "\"," +
                "\"securityEventType\":\"" + securityEventType + "\"}";

        when(objectMapper.readValue(eq(eventJson), any(Class.class)))
                .thenReturn(event);
        when(notificationService.sendNotification(any()))
                .thenReturn(null);

        // When
        eventListener.consumeSecurityEvents(eventJson);

        // Then
        verify(notificationService).sendNotification(requestCaptor.capture());
        SendNotificationRequest request = requestCaptor.getValue();

        assertThat(request.getUserId()).isEqualTo(userId);
        assertThat(request.getTemplateCode()).isEqualTo("security_login");
        assertThat(request.getParameters()).containsEntry("eventTime", eventTime);
        assertThat(request.getParameters()).containsEntry("ipAddress", ipAddress);
        assertThat(request.getParameters()).containsEntry("deviceInfo", deviceInfo);
        // For suspicious activities, both APP and EMAIL channels should be used
        assertThat(request.getTypes()).contains("APP", "EMAIL");
    }

    @Test
    void malformedEvent_ShouldLogErrorAndNotThrowException() throws Exception {
        // Given
        String malformedJson = "{\"eventType\":\"USER_REGISTERED\", \"userId\":\"invalid-uuid\", bad json}";

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
        String unknownEventJson = "{\"eventType\":\"UNKNOWN_EVENT\",\"userId\":\"1234\"}";
        NotificationEvent unknownEvent = mock(NotificationEvent.class);
        when(unknownEvent.getEventType()).thenReturn("UNKNOWN_EVENT");

        when(objectMapper.readValue(eq(unknownEventJson), any(Class.class)))
                .thenReturn(unknownEvent);

        // When
        eventListener.consumeUserEvents(unknownEventJson);

        // Then
        verify(notificationService, never()).sendNotification(any());
    }
}