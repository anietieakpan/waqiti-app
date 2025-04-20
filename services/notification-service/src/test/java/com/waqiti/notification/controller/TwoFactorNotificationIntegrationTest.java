/**
 * File: src/test/java/com/waqiti/notification/controller/TwoFactorNotificationIntegrationTest.java
 * Integration tests for Two Factor Notification Controller
 */
package com.waqiti.notification.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waqiti.notification.domain.DeliveryStatus;
import com.waqiti.notification.domain.Notification;
import com.waqiti.notification.domain.NotificationTemplate;
import com.waqiti.notification.domain.NotificationType;
import com.waqiti.notification.dto.TwoFactorNotificationRequest;
import com.waqiti.notification.repository.NotificationRepository;
import com.waqiti.notification.repository.NotificationTemplateRepository;
import com.waqiti.notification.service.provider.SmsProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertyExtensions;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertyExtensions(properties = {
        "spring.datasource.url=jdbc:tc:postgresql:13:///testdb",
        "twilio.enabled=false"
})
class TwoFactorNotificationIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private NotificationRepository notificationRepository;

    @MockBean
    private SmsProvider smsProvider;

    @Autowired
    private NotificationTemplateRepository templateRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private NotificationTemplate smsTemplate;
    private NotificationTemplate emailTemplate;

    @BeforeEach
    void setup() {
        // Clear notifications
        notificationRepository.deleteAll();

        // Create templates for 2FA
        smsTemplate = createSmsTemplate();
        emailTemplate = createEmailTemplate();

        // Save templates
        smsTemplate = templateRepository.save(smsTemplate);
        emailTemplate = templateRepository.save(emailTemplate);

        // Mock SMS provider
        when(smsProvider.sendSms(anyString(), anyString())).thenReturn("SMS12345");
    }

    @Test
    @WithMockUser(roles = "SERVICE")
    @DisplayName("Should send 2FA SMS successfully")
    void testSendTwoFactorSms() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        TwoFactorNotificationRequest request = TwoFactorNotificationRequest.builder()
                .userId(userId)
                .recipient("+1234567890")
                .verificationCode("123456")
                .language("en")
                .build();

        // When
        MvcResult result = mockMvc.perform(post("/api/v1/notifications/2fa/sms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        boolean success = Boolean.parseBoolean(result.getResponse().getContentAsString());
        assertTrue(success);

        // Verify SMS provider was called
        verify(smsProvider).sendSms(eq("+1234567890"), contains("123456"));

        // Verify notification was created
        List<Notification> notifications = notificationRepository.findAll();
        assertFalse(notifications.isEmpty());

        Notification notification = notifications.get(0);
        assertEquals(userId, notification.getUserId());
        assertEquals(NotificationType.SMS, notification.getType());
        assertEquals("SECURITY", notification.getCategory());
        assertEquals(DeliveryStatus.SENT, notification.getDeliveryStatus());
    }

    @Test
    @WithMockUser(roles = "SERVICE")
    @DisplayName("Should send 2FA email successfully")
    void testSendTwoFactorEmail() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        TwoFactorNotificationRequest request = TwoFactorNotificationRequest.builder()
                .userId(userId)
                .recipient("user@example.com")
                .verificationCode("123456")
                .language("en")
                .build();

        // When
        MvcResult result = mockMvc.perform(post("/api/v1/notifications/2fa/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        boolean success = Boolean.parseBoolean(result.getResponse().getContentAsString());
        assertTrue(success);

        // Verify notification was created
        List<Notification> notifications = notificationRepository.findAll();
        assertFalse(notifications.isEmpty());

        // Filter for EMAIL notifications
        List<Notification> emailNotifications = notifications.stream()
                .filter(n -> n.getType() == NotificationType.EMAIL)
                .collect(Collectors.toList());

        assertFalse(emailNotifications.isEmpty());
        Notification notification = emailNotifications.get(0);
        assertEquals(userId, notification.getUserId());
        assertEquals("SECURITY", notification.getCategory());
    }

    @Test
    @DisplayName("Should reject access without SERVICE role")
    void testUnauthorizedAccessToTwoFactorEndpoints() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        TwoFactorNotificationRequest request = TwoFactorNotificationRequest.builder()
                .userId(userId)
                .recipient("+1234567890")
                .verificationCode("123456")
                .language("en")
                .build();

        // When/Then - Without SERVICE role
        mockMvc.perform(post("/api/v1/notifications/2fa/sms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());

        mockMvc.perform(post("/api/v1/notifications/2fa/email")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "SERVICE")
    @DisplayName("Should return false when SMS delivery fails")
    void testSendTwoFactorSms_SmsFails() throws Exception {
        // Given
        UUID userId = UUID.randomUUID();
        TwoFactorNotificationRequest request = TwoFactorNotificationRequest.builder()
                .userId(userId)
                .recipient("+1234567890")
                .verificationCode("123456")
                .language("en")
                .build();

        // Mock SMS failure
        when(smsProvider.sendSms(anyString(), anyString())).thenReturn(null);

        // When
        MvcResult result = mockMvc.perform(post("/api/v1/notifications/2fa/sms")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        boolean success = Boolean.parseBoolean(result.getResponse().getContentAsString());
        assertFalse(success);

        // Verify notification was created but marked as failed
        List<Notification> notifications = notificationRepository.findAll();
        assertFalse(notifications.isEmpty());

        Notification notification = notifications.get(notifications.size() - 1);
        assertEquals(DeliveryStatus.FAILED, notification.getDeliveryStatus());
    }

    private NotificationTemplate createSmsTemplate() {
        NotificationTemplate template = NotificationTemplate.create(
                "two_factor_code", "2FA Verification Code", "SECURITY",
                "Verification Code", "Your verification code is: ${code}");
        template.setSmsTemplate("Your Waqiti verification code is: ${code}");
        return template;
    }

    private NotificationTemplate createEmailTemplate() {
        NotificationTemplate template = NotificationTemplate.create(
                "two_factor_email", "2FA Email Verification Code", "SECURITY",
                "Verification Code", "Your verification code is: ${code}");
        template.setEmailTemplates(
                "Security Verification Code",
                "<div>Your verification code for ${email} is: <strong>${code}</strong></div>");
        return template;
    }
}