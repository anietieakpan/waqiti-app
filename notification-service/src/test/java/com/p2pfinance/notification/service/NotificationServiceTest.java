/**
 * File: src/test/java/com/p2pfinance/notification/service/NotificationServiceTest.java
 */
package com.p2pfinance.notification.service;

import com.p2pfinance.notification.domain.*;
import com.p2pfinance.notification.dto.NotificationListResponse;
import com.p2pfinance.notification.dto.NotificationResponse;
import com.p2pfinance.notification.dto.SendNotificationRequest;
import com.p2pfinance.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@Tag("UnitTest")
class NotificationServiceTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationTemplateService templateService;

    @Mock
    private NotificationPreferencesService preferencesService;

    @Mock
    private NotificationSenderService senderService;

    @Captor
    private ArgumentCaptor<Notification> notificationCaptor;

    private NotificationService notificationService;

    private UUID userId;
    private NotificationTemplate template;

    @BeforeEach
    void setUp() {
        notificationService = new NotificationService(
                notificationRepository,
                templateService,
                preferencesService,
                senderService
        );

        userId = UUID.randomUUID();

        // Setup a test template
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
    }

    @Test
    void sendNotification_ShouldSendToAllEnabledChannels() {
        // Given
        Map<String, Object> params = Map.of("username", "testuser");

        SendNotificationRequest request = SendNotificationRequest.builder()
                .userId(userId)
                .templateCode("test_template")
                .parameters(params)
                .build();

        when(templateService.getTemplateByCode("test_template")).thenReturn(template);
        when(templateService.renderTemplate(anyString(), eq(params)))
                .thenAnswer(invocation -> {
                    String template = invocation.getArgument(0);
                    if (template.contains("${username}")) {
                        return template.replace("${username}", "testuser");
                    }
                    return template;
                });

        // Mock that all notification types are enabled
        when(preferencesService.isNotificationEnabled(eq(userId), eq("TEST"), any(NotificationType.class)))
                .thenReturn(true);

        // Mock successful sending for all channels
        when(senderService.sendEmailNotification(any(), anyString(), anyString())).thenReturn(true);
        when(senderService.sendSmsNotification(any(), anyString())).thenReturn(true);
        when(senderService.sendPushNotification(any())).thenReturn(true);

        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<NotificationResponse> responses = notificationService.sendNotification(request);

        // Then
        assertThat(responses).hasSize(4); // APP, EMAIL, SMS, PUSH

        verify(notificationRepository, times(4)).save(notificationCaptor.capture());

        List<Notification> savedNotifications = notificationCaptor.getAllValues();
        assertThat(savedNotifications).hasSize(4);

        // Verify APP notification
        Notification appNotification = savedNotifications.stream()
                .filter(n -> n.getType() == NotificationType.APP)
                .findFirst()
                .orElseThrow();

        assertThat(appNotification.getUserId()).isEqualTo(userId);
        assertThat(appNotification.getTitle()).isEqualTo("Hello testuser");
        assertThat(appNotification.getMessage()).isEqualTo("This is a test message for testuser");
        assertThat(appNotification.getCategory()).isEqualTo("TEST");
        assertThat(appNotification.getActionUrl()).isEqualTo("/action/testuser");
        assertThat(appNotification.getDeliveryStatus()).isEqualTo(DeliveryStatus.SENT);

        // Verify EMAIL notification was sent
        verify(senderService).sendEmailNotification(
                any(Notification.class),
                eq("Email subject for testuser"),
                eq("Email body for testuser")
        );

        // Verify SMS notification was sent
        verify(senderService).sendSmsNotification(
                any(Notification.class),
                eq("SMS for testuser")
        );

        // Verify PUSH notification was sent
        verify(senderService).sendPushNotification(any(Notification.class));
    }

    @Test
    void sendNotification_ShouldRespectUserPreferences() {
        // Given
        Map<String, Object> params = Map.of("username", "testuser");

        SendNotificationRequest request = SendNotificationRequest.builder()
                .userId(userId)
                .templateCode("test_template")
                .parameters(params)
                .build();

        when(templateService.getTemplateByCode("test_template")).thenReturn(template);
        when(templateService.renderTemplate(anyString(), eq(params)))
                .thenAnswer(invocation -> {
                    String template = invocation.getArgument(0);
                    if (template.contains("${username}")) {
                        return template.replace("${username}", "testuser");
                    }
                    return template;
                });

        // Mock that only APP and EMAIL are enabled
        when(preferencesService.isNotificationEnabled(userId, "TEST", NotificationType.APP))
                .thenReturn(true);
        when(preferencesService.isNotificationEnabled(userId, "TEST", NotificationType.EMAIL))
                .thenReturn(true);
        when(preferencesService.isNotificationEnabled(userId, "TEST", NotificationType.SMS))
                .thenReturn(false);
        when(preferencesService.isNotificationEnabled(userId, "TEST", NotificationType.PUSH))
                .thenReturn(false);

        when(senderService.sendEmailNotification(any(), anyString(), anyString())).thenReturn(true);
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        List<NotificationResponse> responses = notificationService.sendNotification(request);

        // Then
        assertThat(responses).hasSize(2); // Only APP and EMAIL

        verify(notificationRepository, times(2)).save(any());
        verify(senderService).sendEmailNotification(any(), anyString(), anyString());
        verify(senderService, never()).sendSmsNotification(any(), anyString());
        verify(senderService, never()).sendPushNotification(any());
    }

    @Test
    void getNotifications_ShouldReturnPaginatedNotifications() {
        // Given
        Notification notification1 = Notification.create(
                userId, "Title 1", "Message 1", NotificationType.APP, "TEST");
        Notification notification2 = Notification.create(
                userId, "Title 2", "Message 2", NotificationType.APP, "TEST");

        List<Notification> notifications = List.of(notification1, notification2);
        Page<Notification> page = new PageImpl<>(notifications);

        when(notificationRepository.findByUserIdOrderByCreatedAtDesc(eq(userId), any(Pageable.class)))
                .thenReturn(page);
        when(notificationRepository.countByUserIdAndReadFalse(userId)).thenReturn(2L);

        Pageable pageable = Pageable.unpaged();

        // When
        var response = notificationService.getNotifications(userId, pageable);

        // Then
        assertThat(response.getNotifications()).hasSize(2);
        assertThat(response.getUnreadCount()).isEqualTo(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
    }

    @Test
    void getUnreadNotifications_ShouldReturnOnlyUnreadNotifications() {
        // Given
        Notification notification1 = Notification.create(
                userId, "Title 1", "Message 1", NotificationType.APP, "TEST");

        List<Notification> notifications = List.of(notification1);
        Page<Notification> page = new PageImpl<>(notifications);

        when(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(eq(userId), any(Pageable.class)))
                .thenReturn(page);

        Pageable pageable = Pageable.unpaged();

        // When
        NotificationListResponse response = notificationService.getUnreadNotifications(userId, pageable);

        // Then
        assertThat(response.getNotifications()).hasSize(1);
        assertThat(response.getUnreadCount()).isEqualTo(1);
    }

    @Test
    void markAsRead_ShouldUpdateNotificationStatus() {
        // Given
        UUID notificationId = UUID.randomUUID();
        Notification notification = Notification.create(
                userId, "Title", "Message", NotificationType.APP, "TEST");

        when(notificationRepository.findById(notificationId)).thenReturn(Optional.of(notification));
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        notificationService.markAsRead(notificationId);

        // Then
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification savedNotification = notificationCaptor.getValue();

        assertThat(savedNotification.isRead()).isTrue();
        assertThat(savedNotification.getReadAt()).isNotNull();
    }

    @Test
    void markAllAsRead_ShouldUpdateAllUnreadNotifications() {
        // Given
        Notification notification1 = Notification.create(
                userId, "Title 1", "Message 1", NotificationType.APP, "TEST");
        Notification notification2 = Notification.create(
                userId, "Title 2", "Message 2", NotificationType.APP, "TEST");

        List<Notification> notifications = List.of(notification1, notification2);
        Page<Notification> page = new PageImpl<>(notifications);

        when(notificationRepository.findByUserIdAndReadFalseOrderByCreatedAtDesc(
                eq(userId), any(Pageable.class))).thenReturn(page);
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        notificationService.markAllAsRead(userId);

        // Then
        verify(notificationRepository, times(2)).save(notificationCaptor.capture());

        List<Notification> savedNotifications = notificationCaptor.getAllValues();
        for (Notification notification : savedNotifications) {
            assertThat(notification.isRead()).isTrue();
            assertThat(notification.getReadAt()).isNotNull();
        }
    }

    @Test
    void retryFailedNotifications_ShouldRetryAndUpdateStatus() {
        // Given
        Notification notification = Notification.create(
                userId, "Title", "Message", NotificationType.EMAIL, "TEST");
        notification.updateDeliveryStatus(DeliveryStatus.FAILED, "Failed to send");

        when(notificationRepository.findByDeliveryStatus(DeliveryStatus.FAILED))
                .thenReturn(List.of(notification));
        when(senderService.sendEmailNotification(eq(notification), isNull(), isNull()))
                .thenReturn(true);
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        notificationService.retryFailedNotifications();

        // Then
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification savedNotification = notificationCaptor.getValue();

        assertThat(savedNotification.getDeliveryStatus()).isEqualTo(DeliveryStatus.SENT);
        assertThat(savedNotification.getDeliveryError()).isNull();
    }

    @Test
    void cleanupExpiredNotifications_ShouldUpdateExpiredStatus() {
        // Given
        Notification notification = Notification.create(
                userId, "Title", "Message", NotificationType.APP, "TEST");
        notification.setExpiryDate(LocalDateTime.now().minusDays(1));

        when(notificationRepository.findByReadFalseAndExpiresAtBeforeAndDeliveryStatus(
                any(LocalDateTime.class), eq(DeliveryStatus.SENT)))
                .thenReturn(List.of(notification));
        when(notificationRepository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        notificationService.cleanupExpiredNotifications();

        // Then
        verify(notificationRepository).save(notificationCaptor.capture());
        Notification savedNotification = notificationCaptor.getValue();

        assertThat(savedNotification.getDeliveryStatus()).isEqualTo(DeliveryStatus.EXPIRED);
    }
}