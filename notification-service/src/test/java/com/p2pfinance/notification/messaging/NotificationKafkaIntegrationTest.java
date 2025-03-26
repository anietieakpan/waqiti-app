/**
 * File: src/test/java/com/p2pfinance/notification/messaging/NotificationKafkaIntegrationTest.java
 */
package com.p2pfinance.notification.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p2pfinance.notification.TestcontainersBase;
import com.p2pfinance.notification.domain.Notification;
import com.p2pfinance.notification.domain.NotificationType;
import com.p2pfinance.notification.domain.NotificationTemplate;
import com.p2pfinance.notification.event.PaymentRequestEvent;
import com.p2pfinance.notification.event.UserRegisteredEvent;
import com.p2pfinance.notification.event.WalletTransactionEvent;
import com.p2pfinance.notification.repository.NotificationRepository;
import com.p2pfinance.notification.repository.NotificationTemplateRepository;
import com.p2pfinance.notification.service.NotificationSenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
@Tag("IntegrationTest")
public class NotificationKafkaIntegrationTest extends TestcontainersBase {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationTemplateRepository templateRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationSenderService senderService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        // Clear notifications before each test
        notificationRepository.deleteAll();
        templateRepository.deleteAll();

        userId = UUID.randomUUID();

        // Create template for user registration
        NotificationTemplate userRegisteredTemplate = NotificationTemplate.create(
                "user_registered",
                "User Registration",
                "ACCOUNT",
                "Welcome to P2P Finance, ${username}!",
                "Thank you for registering with P2P Finance. Your account has been created successfully."
        );
        templateRepository.save(userRegisteredTemplate);

        // Create template for payment requests
        NotificationTemplate paymentRequestTemplate = NotificationTemplate.create(
                "payment_request_created",
                "Payment Request Created",
                "PAYMENT_REQUEST",
                "New Payment Request from ${requestorName}",
                "${requestorName} has requested ${amount} ${currency} from you."
        );
        templateRepository.save(paymentRequestTemplate);

        // Create template for wallet transactions
        NotificationTemplate walletDepositTemplate = NotificationTemplate.create(
                "wallet_deposit",
                "Wallet Deposit",
                "TRANSACTION",
                "Deposit Completed: ${amount} ${currency}",
                "Your wallet has been credited with ${amount} ${currency}. New balance: ${newBalance} ${currency}"
        );
        templateRepository.save(walletDepositTemplate);

        // Mock sender service to always return success
        when(senderService.sendEmailNotification(any(), any(), any())).thenReturn(true);
        when(senderService.sendSmsNotification(any(), any())).thenReturn(true);
        when(senderService.sendPushNotification(any())).thenReturn(true);
    }

    @Test
    void userRegisteredEvent_ShouldTriggerNotification() throws Exception {
        // Given
        UserRegisteredEvent event = new UserRegisteredEvent(
                userId,
                "john.doe",
                "john.doe@example.com"
        );

        String eventJson = objectMapper.writeValueAsString(event);

        // When
        kafkaTemplate.send("user-events", eventJson);

        // Then
        // Wait for the notification to be processed and saved
        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                    userId, Pageable.unpaged()).getContent();

            assertThat(notifications).isNotEmpty();

            // Verify notification details
            Notification notification = notifications.get(0);
            assertThat(notification.getUserId()).isEqualTo(userId);
            assertThat(notification.getTitle()).contains("Welcome to P2P Finance, john.doe!");
            assertThat(notification.getType()).isEqualTo(NotificationType.EMAIL);
            assertThat(notification.getCategory()).isEqualTo("ACCOUNT");
        });
    }

    @Test
    void paymentRequestEvent_ShouldTriggerNotification() throws Exception {
        // Given
        UUID requestId = UUID.randomUUID();
        String requestorName = "Jane Doe";
        BigDecimal amount = new BigDecimal("50.00");
        String currency = "USD";

        PaymentRequestEvent event = PaymentRequestEvent.builder()
                .userId(userId)
                .requestId(requestId)
                .status("CREATED")
                .requestorName(requestorName)
                .amount(amount)
                .currency(currency)
                .build();

        String eventJson = objectMapper.writeValueAsString(event);

        // When
        kafkaTemplate.send("payment-request-events", eventJson);

        // Then
        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                    userId, Pageable.unpaged()).getContent();

            assertThat(notifications).isNotEmpty();

            Notification notification = notifications.get(0);
            assertThat(notification.getUserId()).isEqualTo(userId);
            assertThat(notification.getTitle()).contains("New Payment Request from Jane Doe");
            assertThat(notification.getMessage()).contains("50.00 USD");
            assertThat(notification.getReferenceId()).isEqualTo(requestId.toString());
            assertThat(notification.getCategory()).isEqualTo("PAYMENT_REQUEST");
        });
    }

    @Test
    void walletTransactionEvent_ShouldTriggerNotification() throws Exception {
        // Given
        UUID walletId = UUID.randomUUID();
        UUID transactionId = UUID.randomUUID();
        BigDecimal amount = new BigDecimal("100.00");
        String currency = "EUR";
        BigDecimal newBalance = new BigDecimal("250.00");

        WalletTransactionEvent event = WalletTransactionEvent.builder()
                .userId(userId)
                .walletId(walletId)
                .transactionId(transactionId)
                .transactionType("DEPOSIT")
                .amount(amount)
                .currency(currency)
                .newBalance(newBalance)
                .build();

        String eventJson = objectMapper.writeValueAsString(event);

        // When
        kafkaTemplate.send("wallet-events", eventJson);

        // Then
        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                    userId, Pageable.unpaged()).getContent();

            assertThat(notifications).isNotEmpty();

            Notification notification = notifications.get(0);
            assertThat(notification.getUserId()).isEqualTo(userId);
            assertThat(notification.getTitle()).contains("Deposit Completed");
            assertThat(notification.getMessage()).contains("100.00 EUR");
            assertThat(notification.getMessage()).contains("250.00 EUR");
            assertThat(notification.getReferenceId()).isEqualTo(transactionId.toString());
            assertThat(notification.getCategory()).isEqualTo("TRANSACTION");
        });
    }

    @Test
    void malformedEvent_ShouldNotCauseError() throws Exception {
        // Given
        String malformedJson = "{\"eventType\":\"USER_REGISTERED\", \"userId\":\"" + userId + "\", bad json}";

        // When
        kafkaTemplate.send("user-events", malformedJson);

        // Then
        // Wait a bit to ensure message is processed
        TimeUnit.SECONDS.sleep(5);

        // Verify no notifications were created
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                userId, Pageable.unpaged()).getContent();
        assertThat(notifications).isEmpty();
    }

    @Test
    void multipleEvents_ShouldCreateMultipleNotifications() throws Exception {
        // Given
        // First event
        UserRegisteredEvent userEvent = new UserRegisteredEvent(
                userId,
                "john.doe",
                "john.doe@example.com"
        );

        // Second event
        UUID requestId = UUID.randomUUID();
        PaymentRequestEvent paymentEvent = PaymentRequestEvent.builder()
                .userId(userId)
                .requestId(requestId)
                .status("CREATED")
                .requestorName("Jane Doe")
                .amount(new BigDecimal("75.00"))
                .currency("USD")
                .build();

        // When
        kafkaTemplate.send("user-events", objectMapper.writeValueAsString(userEvent));
        kafkaTemplate.send("payment-request-events", objectMapper.writeValueAsString(paymentEvent));

        // Then
        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                    userId, Pageable.unpaged()).getContent();

            assertThat(notifications).hasSize(2);

            // Notifications should have different categories
            assertThat(notifications.stream().map(Notification::getCategory).distinct()).hasSize(2);
            assertThat(notifications.stream().map(Notification::getCategory))
                    .contains("ACCOUNT", "PAYMENT_REQUEST");
        });
    }
}