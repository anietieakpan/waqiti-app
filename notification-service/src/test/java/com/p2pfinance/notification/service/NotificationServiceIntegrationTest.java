/**
 * File: src/test/java/com/p2pfinance/notification/service/NotificationServiceIntegrationTest.java
 */
package com.p2pfinance.notification.service;

import com.p2pfinance.notification.TestcontainersBase;
import com.p2pfinance.notification.domain.*;
import com.p2pfinance.notification.dto.NotificationListResponse;
import com.p2pfinance.notification.dto.NotificationResponse;
import com.p2pfinance.notification.dto.SendNotificationRequest;
import com.p2pfinance.notification.repository.NotificationPreferencesRepository;
import com.p2pfinance.notification.repository.NotificationRepository;
import com.p2pfinance.notification.repository.NotificationTemplateRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@DirtiesContext
@Tag("IntegrationTest")
public class NotificationServiceIntegrationTest extends TestcontainersBase {

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private NotificationTemplateRepository templateRepository;

    @Autowired
    private NotificationPreferencesRepository preferencesRepository;

    @MockBean
    private NotificationSenderService senderService;

    private UUID userId;
    private NotificationTemplate template;

    @BeforeEach
    void setUp() {
        // Clear database before each test
        notificationRepository.deleteAll();

        userId = UUID.randomUUID();

        // Create notification preferences
        NotificationPreferences preferences = NotificationPreferences.createDefault(userId);
        preferences.updateContactInfo("test@example.com", "+1234567890", "device-token-123");
        preferencesRepository.save(preferences);

        // Create test template
        template = NotificationTemplate.create(
                "test_template",
                "Test Template",
                "TEST",
                "Hello ${username}",
                "This is a test message for ${username}"
        );
        template.setEmailTemplates(
                "Email subject for ${username}",
                "Email body for ${username}"
        );
        template.setSmsTemplate("SMS for ${username}");
        template.setActionUrlTemplate("/action/${username}");
        templateRepository.save(template);

        // Mock sender service
        when(senderService.sendEmailNotification(any(), anyString(), anyString())).thenReturn(true);
        when(senderService.sendSmsNotification(any(), anyString())).thenReturn(true);
        when(senderService.sendPushNotification(any())).thenReturn(true);
    }

    @Test
    @Transactional
    void sendNotification_ShouldCreateAndPersistNotifications() {
        // Given
        Map<String, Object> params = new HashMap<>();
        params.put("username", "testuser");

        SendNotificationRequest request = SendNotificationRequest.builder()
                .userId(userId)
                .templateCode("test_template")
                .parameters(params)
                .build();

        // When
        List<NotificationResponse> responses = notificationService.sendNotification(request);

        // Then
        assertThat(responses).isNotEmpty();

        // Check that notifications were persisted in the database
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                userId, Pageable.unpaged()).getContent();

        assertThat(notifications).isNotEmpty();

        // Verify APP notification details
        Notification appNotification = notifications.stream()
                .filter(n -> n.getType() == NotificationType.APP)
                .findFirst()
                .orElseThrow();

        assertThat(appNotification.getUserId()).isEqualTo(userId);
        assertThat(appNotification.getTitle()).isEqualTo("Hello testuser");
        assertThat(appNotification.getMessage()).isEqualTo("This is a test message for testuser");
        assertThat(appNotification.getActionUrl()).isEqualTo("/action/testuser");
    }

    @Test
    @Transactional
    void getNotifications_ShouldReturnPaginatedResults() {
        // Given - create multiple notifications
        for (int i = 0; i < 10; i++) {
            Notification notification = Notification.create(
                    userId,
                    "Title " + i,
                    "Message " + i,
                    NotificationType.APP,
                    "TEST"
            );
            notificationRepository.save(notification);
        }

        // When - get first page (5 items)
        Pageable pageable = PageRequest.of(0, 5);
        NotificationListResponse response = notificationService.getNotifications(userId, pageable);

        // Then
        assertThat(response.getNotifications()).hasSize(5);
        assertThat(response.getTotalElements()).isEqualTo(10);
        assertThat(response.getTotalPages()).isEqualTo(2);

        // When - get second page
        pageable = PageRequest.of(1, 5);
        response = notificationService.getNotifications(userId, pageable);

        // Then
        assertThat(response.getNotifications()).hasSize(5);
        assertThat(response.getPage()).isEqualTo(1);
    }

    @Test
    @Transactional
    void getUnreadNotifications_ShouldReturnOnlyUnread() {
        // Given - create mix of read and unread notifications
        for (int i = 0; i < 5; i++) {
            Notification notification = Notification.create(
                    userId,
                    "Unread " + i,
                    "Unread message " + i,
                    NotificationType.APP,
                    "TEST"
            );
            notificationRepository.save(notification);
        }

        for (int i = 0; i < 3; i++) {
            Notification notification = Notification.create(
                    userId,
                    "Read " + i,
                    "Read message " + i,
                    NotificationType.APP,
                    "TEST"
            );
            notification.markAsRead();
            notificationRepository.save(notification);
        }

        // When
        Pageable pageable = PageRequest.of(0, 10);
        NotificationListResponse response = notificationService.getUnreadNotifications(userId, pageable);

        // Then
        assertThat(response.getNotifications()).hasSize(5);
        assertThat(response.getUnreadCount()).isEqualTo(5);

        for (NotificationResponse notificationResponse : response.getNotifications()) {
            assertThat(notificationResponse.isRead()).isFalse();
        }
    }

    @Test
    @Transactional
    void markAsRead_ShouldUpdateReadStatusInDatabase() {
        // Given
        Notification notification = Notification.create(
                userId,
                "Test Title",
                "Test Message",
                NotificationType.APP,
                "TEST"
        );
        notification = notificationRepository.save(notification);
        UUID notificationId = notification.getId();

        // Verify it's unread initially
        assertThat(notification.isRead()).isFalse();
        assertThat(notification.getReadAt()).isNull();

        // When
        NotificationResponse response = notificationService.markAsRead(notificationId);

        // Then
        assertThat(response.isRead()).isTrue();
        assertThat(response.getReadAt()).isNotNull();

        // Verify database was updated
        Notification updatedNotification = notificationRepository.findById(notificationId).orElseThrow();
        assertThat(updatedNotification.isRead()).isTrue();
        assertThat(updatedNotification.getReadAt()).isNotNull();
    }

    @Test
    @Transactional
    void markAllAsRead_ShouldUpdateAllUnreadNotifications() {
        // Given
        for (int i = 0; i < 5; i++) {
            Notification notification = Notification.create(
                    userId,
                    "Title " + i,
                    "Message " + i,
                    NotificationType.APP,
                    "TEST"
            );
            notificationRepository.save(notification);
        }

        // When
        notificationService.markAllAsRead(userId);

        // Then
        List<Notification> notifications = notificationRepository.findByUserIdOrderByCreatedAtDesc(
                userId, Pageable.unpaged()).getContent();

        for (Notification notification : notifications) {
            assertThat(notification.isRead()).isTrue();
            assertThat(notification.getReadAt()).isNotNull();
        }

        // Verify unread count is zero
        long unreadCount = notificationRepository.countByUserIdAndReadFalse(userId);
        assertThat(unreadCount).isZero();
    }

    @Test
    @Transactional
    void cleanupExpiredNotifications_ShouldUpdateExpiredStatus() {
        // Given
        LocalDateTime yesterday = LocalDateTime.now().minusDays(1);

        // Create expired notification
        Notification expiredNotification = Notification.create(
                userId,
                "Expired Notification",
                "This notification is expired",
                NotificationType.APP,
                "TEST"
        );
        expiredNotification.setExpiryDate(yesterday);
        expiredNotification.updateDeliveryStatus(DeliveryStatus.SENT, null);
        notificationRepository.save(expiredNotification);

        // Create non-expired notification
        Notification activeNotification = Notification.create(
                userId,
                "Active Notification",
                "This notification is not expired",
                NotificationType.APP,
                "TEST"
        );
        activeNotification.setExpiryDate(LocalDateTime.now().plusDays(1));
        activeNotification.updateDeliveryStatus(DeliveryStatus.SENT, null);
        notificationRepository.save(activeNotification);

        // When
        notificationService.cleanupExpiredNotifications();

        // Then
        Notification updatedExpiredNotification = notificationRepository.findById(
                expiredNotification.getId()).orElseThrow();
        Notification updatedActiveNotification = notificationRepository.findById(
                activeNotification.getId()).orElseThrow();

        // Expired notification should be marked as EXPIRED
        assertThat(updatedExpiredNotification.getDeliveryStatus()).isEqualTo(DeliveryStatus.EXPIRED);

        // Active notification should still be SENT
        assertThat(updatedActiveNotification.getDeliveryStatus()).isEqualTo(DeliveryStatus.SENT);
    }
}