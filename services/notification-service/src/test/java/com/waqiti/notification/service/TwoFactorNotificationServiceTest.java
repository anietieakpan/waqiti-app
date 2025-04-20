/**
 * File: src/test/java/com/waqiti/notification/service/TwoFactorNotificationServiceTest.java
 * Unit tests for Two Factor Notification Service
 */
package com.waqiti.notification.service;

import com.waqiti.notification.domain.DeliveryStatus;
import com.waqiti.notification.domain.Notification;
import com.waqiti.notification.domain.NotificationTemplate;
import com.waqiti.notification.domain.NotificationType;
import com.waqiti.notification.dto.NotificationResponse;
import com.waqiti.notification.dto.SendNotificationRequest;
import com.waqiti.notification.repository.NotificationRepository;
import com.waqiti.notification.service.provider.SmsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TwoFactorNotificationServiceTest {
    @Mock
    private NotificationService notificationService;

    @Mock
    private NotificationTemplateService templateService;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private SmsProvider smsProvider;

    @InjectMocks
    private TwoFactorNotificationService twoFactorService;

    private NotificationTemplate createMockTemplate() {
        NotificationTemplate template = NotificationTemplate.create(
                "two_factor_code", "2FA Verification Code", "SECURITY",
                "Verification Code", "Your verification code is: ${code}");
        template.setSmsTemplate("Your Waqiti verification code is: ${code}");
        return template;
    }

    @BeforeEach
    void setup() {
        // Setup common test data
    }

    @Test
    @DisplayName("Should successfully send SMS with 2FA code")
    void testSendTwoFactorSms_Success() {
        // Given
        UUID userId = UUID.randomUUID();
        String phoneNumber = "+1234567890";
        String code = "123456";

        NotificationTemplate template = createMockTemplate();
        when(templateService.getTemplateByCode("two_factor_code")).thenReturn(template);
        when(templateService.renderTemplate(anyString(), anyMap())).thenReturn("Your code is: 123456");
        when(smsProvider.sendSms(eq(phoneNumber), anyString())).thenReturn("SM123456");

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        when(notificationRepository.save(notificationCaptor.capture())).thenAnswer(i -> i.getArgument(0));

        // When
        boolean result = twoFactorService.sendTwoFactorSms(userId, phoneNumber, code);

        // Then
        assertTrue(result);

        // Verify notification was created correctly
        Notification savedNotification = notificationCaptor.getAllValues().get(0);
        assertEquals(userId, savedNotification.getUserId());
        assertEquals(NotificationType.SMS, savedNotification.getType());
        assertEquals("SECURITY", savedNotification.getCategory());

        // Verify SMS was sent
        verify(smsProvider).sendSms(eq(phoneNumber), anyString());

        // Verify status was updated
        verify(notificationRepository, times(2)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should use fallback template when template not found")
    void testSendTwoFactorSms_TemplateNotFound() {
        // Given
        UUID userId = UUID.randomUUID();
        String phoneNumber = "+1234567890";
        String code = "123456";

        when(templateService.getTemplateByCode("two_factor_code"))
                .thenThrow(new IllegalArgumentException("Template not found"));
        when(smsProvider.sendSms(eq(phoneNumber), contains(code))).thenReturn("SM123456");

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        when(notificationRepository.save(notificationCaptor.capture())).thenAnswer(i -> i.getArgument(0));

        // When
        boolean result = twoFactorService.sendTwoFactorSms(userId, phoneNumber, code);

        // Then
        assertTrue(result);

        // Verify SMS was sent with fallback message
        verify(smsProvider).sendSms(eq(phoneNumber), contains(code));

        // Verify notification was created with fallback content
        Notification savedNotification = notificationCaptor.getAllValues().get(0);
        assertEquals(userId, savedNotification.getUserId());
        assertEquals("Your Waqiti verification code is: " + code, savedNotification.getMessage());
    }

    @Test
    @DisplayName("Should return false when SMS delivery fails")
    void testSendTwoFactorSms_SmsFailed() {
        // Given
        UUID userId = UUID.randomUUID();
        String phoneNumber = "+1234567890";
        String code = "123456";

        NotificationTemplate template = createMockTemplate();
        when(templateService.getTemplateByCode("two_factor_code")).thenReturn(template);
        when(templateService.renderTemplate(anyString(), anyMap())).thenReturn("Your code is: 123456");
        when(smsProvider.sendSms(eq(phoneNumber), anyString())).thenReturn(null);

        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        when(notificationRepository.save(notificationCaptor.capture())).thenAnswer(i -> i.getArgument(0));

        // When
        boolean result = twoFactorService.sendTwoFactorSms(userId, phoneNumber, code);

        // Then
        assertFalse(result);

        // Verify notification was created with failed status
        verify(notificationRepository, times(2)).save(any(Notification.class));

        Notification updatedNotification = notificationCaptor.getAllValues().get(1);  // Second save is status update
        assertEquals(DeliveryStatus.FAILED, updatedNotification.getDeliveryStatus());
    }

    @Test
    @DisplayName("Should successfully send 2FA email")
    void testSendTwoFactorEmail() {
        // Given
        UUID userId = UUID.randomUUID();
        String email = "test@example.com";
        String code = "123456";

        Map<String, Object> params = new HashMap<>();
        params.put("code", code);
        params.put("email", email);

        ArgumentCaptor<SendNotificationRequest> requestCaptor =
                ArgumentCaptor.forClass(SendNotificationRequest.class);

        // Mock the notification service to handle the email
        when(notificationService.sendNotification(requestCaptor.capture()))
                .thenReturn(List.of(new NotificationResponse()));

        // When
        boolean result = twoFactorService.sendTwoFactorEmail(userId, email, code);

        // Then
        assertTrue(result);

        // Verify correct request was sent to notification service
        SendNotificationRequest capturedRequest = requestCaptor.getValue();
        assertEquals(userId, capturedRequest.getUserId());
        assertEquals("two_factor_email", capturedRequest.getTemplateCode());
        assertEquals(code, capturedRequest.getParameters().get("code"));
        assertEquals(email, capturedRequest.getParameters().get("email"));
        assertArrayEquals(new String[] { "EMAIL" }, capturedRequest.getTypes());
    }
}