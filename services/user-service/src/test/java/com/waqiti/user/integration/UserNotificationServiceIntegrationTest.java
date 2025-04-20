
/** src/test/java/com/waqiti/user/integration/UserNotificationServiceIntegrationTest.java **/

package com.waqiti.user.integration;

import com.waqiti.user.client.NotificationServiceClient;
import com.waqiti.user.dto.TwoFactorNotificationRequest;
import com.waqiti.user.repository.UserRepository;
import com.waqiti.user.service.MfaService;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import com.waqiti.user.domain.MfaMethod;
import com.waqiti.user.domain.User;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;




@SpringBootTest
class UserNotificationServiceIntegrationTest {
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
        testUser = User.create("integuser", "integration@example.com",
                passwordEncoder.encode("Password123!"), "ext-int-123");
        testUser.activate();
        testUser = userRepository.save(testUser);

        // Mock notification service
        when(notificationServiceClient.sendTwoFactorSms(any())).thenReturn(true);
        when(notificationServiceClient.sendTwoFactorEmail(any())).thenReturn(true);
    }

    @Test
    void testSmsVerificationIntegration() {
        // Given
        UUID userId = testUser.getId();
        String phoneNumber = "+1234567890";

        // When
        boolean setupResult = mfaService.setupSms(userId, phoneNumber);

        // Then
        assertTrue(setupResult);

        // Verify notification service was called with correct parameters
        ArgumentCaptor<TwoFactorNotificationRequest> requestCaptor =
                ArgumentCaptor.forClass(TwoFactorNotificationRequest.class);
        verify(notificationServiceClient).sendTwoFactorSms(requestCaptor.capture());

        TwoFactorNotificationRequest request = requestCaptor.getValue();
        assertEquals(userId, request.getUserId());
        assertEquals(phoneNumber, request.getRecipient());
        assertNotNull(request.getVerificationCode());
        assertEquals("en", request.getLanguage()); // Default language
    }

    @Test
    void testEmailVerificationIntegration() {
        // Given
        UUID userId = testUser.getId();
        String email = "integration@example.com";

        // When
        boolean setupResult = mfaService.setupEmail(userId, email);

        // Then
        assertTrue(setupResult);

        // Verify notification service was called
        ArgumentCaptor<TwoFactorNotificationRequest> requestCaptor =
                ArgumentCaptor.forClass(TwoFactorNotificationRequest.class);
        verify(notificationServiceClient).sendTwoFactorEmail(requestCaptor.capture());

        TwoFactorNotificationRequest request = requestCaptor.getValue();
        assertEquals(userId, request.getUserId());
        assertEquals(email, request.getRecipient());
        assertNotNull(request.getVerificationCode());
    }

    @Test
    void testResendVerificationCode() {
        // Given
        UUID userId = testUser.getId();
        String phoneNumber = "+1234567890";

        // Setup SMS first
        mfaService.setupSms(userId, phoneNumber);

        // Reset mock to verify next call
        reset(notificationServiceClient);
        when(notificationServiceClient.sendTwoFactorSms(any())).thenReturn(true);

        // When
        boolean resendResult = mfaService.resendVerificationCode(userId, MfaMethod.SMS);

        // Then
        assertTrue(resendResult);

        // Verify notification service was called again
        verify(notificationServiceClient).sendTwoFactorSms(any());
    }

    @Test
    void testHandleNotificationServiceFailure() {
        // Given
        UUID userId = testUser.getId();
        String phoneNumber = "+1234567890";

        // Mock notification service failure
        when(notificationServiceClient.sendTwoFactorSms(any())).thenReturn(false);

        // When
        boolean setupResult = mfaService.setupSms(userId, phoneNumber);

        // Then
        assertFalse(setupResult);
    }

    @Test
    void testHandleNotificationServiceException() {
        // Given
        UUID userId = testUser.getId();
        String phoneNumber = "+1234567890";

        // Mock notification service exception
        when(notificationServiceClient.sendTwoFactorSms(any()))
                .thenThrow(new RuntimeException("Service unavailable"));

        // When
        boolean setupResult = mfaService.setupSms(userId, phoneNumber);

        // Then
        assertFalse(setupResult);
    }
}