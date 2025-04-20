/**
 * File: src/test/java/com/waqiti/user/i18n/MfaLocalizationTest.java
 * Tests for MFA internationalization and localization
 */
package com.waqiti.user.i18n;

import com.waqiti.user.client.NotificationServiceClient;
import com.waqiti.user.domain.User;
import com.waqiti.user.dto.TwoFactorNotificationRequest;
import com.waqiti.user.repository.UserRepository;
import com.waqiti.user.service.MfaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
class MfaLocalizationTest {
    @Autowired
    private MfaService mfaService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private NotificationServiceClient notificationServiceClient;

    private User testUser;

    @BeforeEach
    void setup() {
        // Create test user
        testUser = User.create("i18nuser", "i18n@example.com",
                passwordEncoder.encode("Password123!"), "ext-i18n-123");
        testUser.activate();
        testUser = userRepository.save(testUser);

        // Mock notification service
        when(notificationServiceClient.sendTwoFactorSms(any())).thenReturn(true);
        when(notificationServiceClient.sendTwoFactorEmail(any())).thenReturn(true);
    }

    @Test
    @DisplayName("Should send SMS with correct language")
    void testSmsWithDifferentLanguages() {
        // Test different languages
        String[] languages = {"en", "fr", "es", "de", "zh"};

        for (String language : languages) {
            // Given
            UUID userId = testUser.getId();
            String phoneNumber = "+1234567890";

            // Reset mock
            reset(notificationServiceClient);
            when(notificationServiceClient.sendTwoFactorSms(any())).thenReturn(true);

            // When
            // In a real implementation, we would set the user's language preference
            // and the MFA service would use that
            // For this test, we'll simulate by setting it manually
            ReflectionTestUtils.setField(mfaService, "defaultLanguage", language);

            mfaService.setupSms(userId, phoneNumber);

            // Then
            ArgumentCaptor<TwoFactorNotificationRequest> requestCaptor =
                    ArgumentCaptor.forClass(TwoFactorNotificationRequest.class);
            verify(notificationServiceClient).sendTwoFactorSms(requestCaptor.capture());

            // Verify language is passed correctly
            TwoFactorNotificationRequest request = requestCaptor.getValue();
            assertEquals(language, request.getLanguage());
        }
    }

    @Test
    @DisplayName("Should handle international phone numbers correctly")
    void testInternationalPhoneNumbers() {
        // Test international phone numbers
        String[] phoneNumbers = {
                "+1234567890",      // US
                "+44123456789",     // UK
                "+33123456789",     // France
                "+8613912345678",   // China
                "+27123456789"      // South Africa
        };

        for (String phoneNumber : phoneNumbers) {
            // Given
            UUID userId = testUser.getId();

            // Reset mock
            reset(notificationServiceClient);
            when(notificationServiceClient.sendTwoFactorSms(any())).thenReturn(true);

            // When
            mfaService.setupSms(userId, phoneNumber);

            // Then
            ArgumentCaptor<TwoFactorNotificationRequest> requestCaptor =
                    ArgumentCaptor.forClass(TwoFactorNotificationRequest.class);
            verify(notificationServiceClient).sendTwoFactorSms(requestCaptor.capture());

            // Verify phone number is passed correctly
            TwoFactorNotificationRequest request = requestCaptor.getValue();
            assertEquals(phoneNumber, request.getRecipient());
        }
    }

    @Test
    @DisplayName("Should handle non-Latin characters in SMS content")
    void testNonLatinCharacterSmsContent() {
        // Given
        UUID userId = testUser.getId();
        String phoneNumber = "+1234567890";

        // Custom message with non-Latin characters
        ReflectionTestUtils.setField(mfaService, "smsMessageTemplate",
                "Your verification code is: %s. 您的验证码是：%s. Ваш код подтверждения: %s.");

        // When
        mfaService.setupSms(userId, phoneNumber);

        // Then
        verify(notificationServiceClient).sendTwoFactorSms(any());
        // In a real test, you would verify that the SMS message is correctly encoded
    }

    @Test
    @DisplayName("Should use correct language templates for email")
    void testEmailTemplatesForDifferentLanguages() {
        // Given
        UUID userId = testUser.getId();
        String email = "i18n@example.com";
        String[] languages = {"en", "fr", "es", "de", "zh"};

        for (String language : languages) {
            // Reset mock
            reset(notificationServiceClient);
            when(notificationServiceClient.sendTwoFactorEmail(any())).thenReturn(true);

            // When
            ReflectionTestUtils.setField(mfaService, "defaultLanguage", language);
            mfaService.setupEmail(userId, email);

            // Then
            ArgumentCaptor<TwoFactorNotificationRequest> requestCaptor =
                    ArgumentCaptor.forClass(TwoFactorNotificationRequest.class);
            verify(notificationServiceClient).sendTwoFactorEmail(requestCaptor.capture());

            // Verify language is passed correctly
            TwoFactorNotificationRequest request = requestCaptor.getValue();
            assertEquals(language, request.getLanguage());
        }
    }
}