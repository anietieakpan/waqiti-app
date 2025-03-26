/**
 * File: src/test/java/com/p2pfinance/notification/NotificationE2ETest.java
 */
package com.p2pfinance.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.p2pfinance.notification.domain.*;
import com.p2pfinance.notification.dto.UpdatePreferencesRequest;
import com.p2pfinance.notification.event.PaymentRequestEvent;
import com.p2pfinance.notification.repository.NotificationPreferencesRepository;
import com.p2pfinance.notification.repository.NotificationRepository;
import com.p2pfinance.notification.repository.NotificationTemplateRepository;
import com.p2pfinance.notification.service.NotificationSenderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;
import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@DirtiesContext
@Tag("IntegrationTest")
public class NotificationE2ETest extends TestcontainersBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationTemplateRepository templateRepository;

    @Autowired
    private NotificationPreferencesRepository preferencesRepository;

    @MockBean
    private NotificationSenderService senderService;

    private UUID userId;

    @BeforeEach
    void setUp() {
        // Clear data
        notificationRepository.deleteAll();
        templateRepository.deleteAll();

        userId = UUID.randomUUID();

        // Setup notification preferences
        NotificationPreferences preferences = NotificationPreferences.createDefault(userId);
        preferences.updateContactInfo("test@example.com", "+1234567890", "device-token-123");
        preferencesRepository.save(preferences);

        // Create templates
        NotificationTemplate paymentRequestTemplate = NotificationTemplate.create(
                "payment_request_created",
                "Payment Request Created",
                "PAYMENT_REQUEST",
                "New Payment Request from ${requestorName}",
                "${requestorName} has requested ${amount} ${currency} from you."
        );
        paymentRequestTemplate.setEmailTemplates(
                "Payment Request from ${requestorName}",
                "<p>Dear user,</p><p>${requestorName} has requested ${amount} ${currency} from you.</p>"
        );
        paymentRequestTemplate.setActionUrlTemplate("/payment-requests/${requestId}");
        templateRepository.save(paymentRequestTemplate);

        // Mock sender service
        when(senderService.sendEmailNotification(any(), any(), any())).thenReturn(true);
        when(senderService.sendSmsNotification(any(), any())).thenReturn(true);
        when(senderService.sendPushNotification(any())).thenReturn(true);
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void completeNotificationFlow_ShouldWork() throws Exception {
        // PART 1: Create a notification via Kafka event
        // Given
        UUID requestId = UUID.randomUUID();
        PaymentRequestEvent event = PaymentRequestEvent.builder()
                .userId(userId)
                .requestId(requestId)
                .status("CREATED")
                .requestorId(UUID.randomUUID())
                .requestorName("Jane Doe")
                .amount(new BigDecimal("50.00"))
                .currency("USD")
                .build();

        String eventJson = objectMapper.writeValueAsString(event);

        // When - send event to Kafka
        kafkaTemplate.send("payment-request-events", eventJson);

        // Then - Wait for notification to be created in DB
        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                    userId, Pageable.unpaged()).getContent();

            assertThat(notifications).isNotEmpty();

            // Verify notification details
            Notification notification = notifications.get(0);
            assertThat(notification.getUserId()).isEqualTo(userId);
            assertThat(notification.getTitle()).contains("New Payment Request from Jane Doe");
            assertThat(notification.getMessage()).contains("Jane Doe has requested 50.00 USD from you");
            assertThat(notification.getReferenceId()).isEqualTo(requestId.toString());
            assertThat(notification.getActionUrl()).isEqualTo("/payment-requests/" + requestId);
        });

        // PART 2: Retrieve the notification via API
        // When & Then - Check API returns the notification
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications", hasSize(greaterThan(0))))
                .andExpect(jsonPath("$.notifications[0].title").value(containsString("New Payment Request from Jane Doe")))
                .andExpect(jsonPath("$.notifications[0].referenceId").value(requestId.toString()));

        // PART 3: Mark the notification as read
        // Get the notification ID
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                userId, Pageable.unpaged()).getContent();
        UUID notificationId = notifications.get(0).getId();

        // When - mark as read
        mockMvc.perform(post("/api/v1/notifications/{id}/read", notificationId)
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.read").value(true))
                .andExpect(jsonPath("$.readAt").isNotEmpty());

        // Then - check database
        Notification updatedNotification = notificationRepository.findById(notificationId).orElseThrow();
        assertThat(updatedNotification.isRead()).isTrue();
        assertThat(updatedNotification.getReadAt()).isNotNull();

        // PART 4: Verify unread count is updated
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(0));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void userPreferences_ShouldAffectNotificationDelivery() throws Exception {
        // PART 1: Update preferences to disable email notifications
        UpdatePreferencesRequest updateRequest = UpdatePreferencesRequest.builder()
                .emailNotificationsEnabled(false)
                .build();

        mockMvc.perform(put("/api/v1/notifications/preferences")
                        .header("Authorization", "Bearer test-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.emailNotificationsEnabled").value(false));

        // PART 2: Send payment request event
        UUID requestId = UUID.randomUUID();
        PaymentRequestEvent event = PaymentRequestEvent.builder()
                .userId(userId)
                .requestId(requestId)
                .status("CREATED")
                .requestorName("Jane Doe")
                .amount(new BigDecimal("50.00"))
                .currency("USD")
                .build();

        String eventJson = objectMapper.writeValueAsString(event);
        kafkaTemplate.send("payment-request-events", eventJson);

        // PART 3: Wait for processing and check notifications
        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                    userId, Pageable.unpaged()).getContent();

            // Should only have APP notifications since EMAIL is disabled
            assertThat(notifications).isNotEmpty();
            assertThat(notifications.stream()
                    .filter(n -> n.getType() == NotificationType.APP)
                    .count()).isEqualTo(1);

            assertThat(notifications.stream()
                    .filter(n -> n.getType() == NotificationType.EMAIL)
                    .count()).isEqualTo(0);
        });
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void multipleNotificationsAndMarkAllAsRead() throws Exception {
        // PART 1: Create multiple notifications via Kafka events
        for (int i = 0; i < 3; i++) {
            PaymentRequestEvent event = PaymentRequestEvent.builder()
                    .userId(userId)
                    .requestId(UUID.randomUUID())
                    .status("CREATED")
                    .requestorName("Requester " + i)
                    .amount(new BigDecimal("10.00"))
                    .currency("USD")
                    .build();

            kafkaTemplate.send("payment-request-events", objectMapper.writeValueAsString(event));
            // Add a small delay to ensure distinct creation times
            TimeUnit.MILLISECONDS.sleep(200);
        }

        // PART 2: Wait for notifications to be created
        await().atMost(30, TimeUnit.SECONDS).untilAsserted(() -> {
            List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                    userId, Pageable.unpaged()).getContent();

            assertThat(notifications).hasSize(3);
        });

        // PART 3: Verify notifications via API
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications", hasSize(3)))
                .andExpect(jsonPath("$.unreadCount").value(3));

        // PART 4: Mark all as read
        mockMvc.perform(post("/api/v1/notifications/read-all")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk());

        // PART 5: Verify all notifications are marked as read
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                userId, Pageable.unpaged()).getContent();

        for (Notification notification : notifications) {
            assertThat(notification.isRead()).isTrue();
            assertThat(notification.getReadAt()).isNotNull();
        }

        // PART 6: Check unread count via API
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(0));
    }

    @Test
    @WithMockUser(username = "test-user-id")
    void testUnreadNotificationsEndpoint() throws Exception {
        // PART 1: Create mix of read and unread notifications
        // Create unread notifications
        for (int i = 0; i < 2; i++) {
            Notification notification = Notification.create(
                    userId,
                    "Unread Notification " + i,
                    "This is unread message " + i,
                    NotificationType.APP,
                    "TEST"
            );
            notificationRepository.save(notification);
        }

        // Create read notifications
        for (int i = 0; i < 3; i++) {
            Notification notification = Notification.create(
                    userId,
                    "Read Notification " + i,
                    "This is read message " + i,
                    NotificationType.APP,
                    "TEST"
            );
            notification.markAsRead();
            notificationRepository.save(notification);
        }

        // PART 2: Verify unread notifications endpoint returns only unread
        mockMvc.perform(get("/api/v1/notifications/unread")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications", hasSize(2)))
                .andExpect(jsonPath("$.unreadCount").value(2))
                .andExpect(jsonPath("$.notifications[0].read").value(false))
                .andExpect(jsonPath("$.notifications[1].read").value(false));

        // PART 3: Verify all notifications endpoint returns all
        mockMvc.perform(get("/api/v1/notifications")
                        .header("Authorization", "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.notifications", hasSize(5)))
                .andExpect(jsonPath("$.unreadCount").value(2));
    }
}