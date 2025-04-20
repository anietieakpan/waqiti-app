/**
 * File: src/test/java/com/waqiti/user/service/MfaServiceTest.java
 * Unit tests for the MFA Service
 */
package com.waqiti.user.service;

import com.waqiti.user.client.NotificationServiceClient;
import com.waqiti.user.domain.MfaConfiguration;
import com.waqiti.user.domain.MfaMethod;
import com.waqiti.user.domain.MfaVerificationCode;
import com.waqiti.user.dto.MfaSetupResponse;
import com.waqiti.user.dto.TwoFactorNotificationRequest;
import com.waqiti.user.repository.MfaConfigurationRepository;
import com.waqiti.user.repository.MfaVerificationCodeRepository;
import dev.samstevens.totp.code.CodeVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MfaServiceTest {
    @Mock
    private MfaConfigurationRepository mfaConfigRepository;

    @Mock
    private MfaVerificationCodeRepository verificationCodeRepository;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @InjectMocks
    private MfaService mfaService;

    @BeforeEach
    void setup() {
        // Initialize any necessary test data or mocks
    }

    @Test
    @DisplayName("Should set up TOTP for a new user")
    void testSetupTotp_NewUser() {
        // Given
        UUID userId = UUID.randomUUID();
        String username = "testuser";
        when(mfaConfigRepository.findByUserIdAndMethod(userId, MfaMethod.TOTP))
                .thenReturn(Optional.empty());

        // When
        MfaSetupResponse response = mfaService.setupTotp(userId, username);

        // Then
        assertNotNull(response.getSecret());
        assertNotNull(response.getQrCodeImage());
        verify(mfaConfigRepository).save(any(MfaConfiguration.class));
    }

    @Test
    @DisplayName("Should update TOTP configuration for existing user")
    void testSetupTotp_ExistingUser() {
        // Given
        UUID userId = UUID.randomUUID();
        String username = "testuser";
        MfaConfiguration existingConfig = MfaConfiguration.create(userId, MfaMethod.TOTP, "OLDSECRET");
        existingConfig.markVerified();
        existingConfig.enable();

        when(mfaConfigRepository.findByUserIdAndMethod(userId, MfaMethod.TOTP))
                .thenReturn(Optional.of(existingConfig));

        // When
        MfaSetupResponse response = mfaService.setupTotp(userId, username);

        // Then
        assertNotNull(response.getSecret());
        assertNotEquals("OLDSECRET", response.getSecret());
        verify(mfaConfigRepository).save(argThat(config ->
                !config.isEnabled() && config.getSecret() != null));
    }

    @Test
    @DisplayName("Should verify TOTP setup with valid code")
    void testVerifyTotpSetup_Success() {
        // Given
        UUID userId = UUID.randomUUID();
        String secret = "TESTR3ALSECRET";
        MfaConfiguration config = MfaConfiguration.create(userId, MfaMethod.TOTP, secret);
        when(mfaConfigRepository.findByUserIdAndMethod(userId, MfaMethod.TOTP))
                .thenReturn(Optional.of(config));

        // Use a code verifier mock
        CodeVerifier codeVerifier = mock(CodeVerifier.class);
        when(codeVerifier.isValidCode(secret, "123456")).thenReturn(true);
        ReflectionTestUtils.setField(mfaService, "codeVerifier", codeVerifier);

        // When
        boolean result = mfaService.verifyTotpSetup(userId, "123456");

        // Then
        assertTrue(result);
        verify(mfaConfigRepository).save(argThat(conf ->
                conf.isEnabled() && conf.isVerified()));
    }

    @Test
    @DisplayName("Should setup SMS MFA and send verification code")
    void testSetupSms_Success() {
        // Given
        UUID userId = UUID.randomUUID();
        String phoneNumber = "+1234567890";
        when(mfaConfigRepository.findByUserIdAndMethod(userId, MfaMethod.SMS))
                .thenReturn(Optional.empty());
        when(notificationServiceClient.sendTwoFactorSms(any())).thenReturn(true);

        // When
        boolean result = mfaService.setupSms(userId, phoneNumber);

        // Then
        assertTrue(result);
        verify(mfaConfigRepository).save(argThat(config ->
                config.getMethod() == MfaMethod.SMS && config.getSecret().equals(phoneNumber)));
        verify(verificationCodeRepository).save(any(MfaVerificationCode.class));
        verify(notificationServiceClient).sendTwoFactorSms(argThat(req ->
                req.getUserId().equals(userId) && req.getRecipient().equals(phoneNumber)));
    }

    @Test
    @DisplayName("Should return enabled MFA methods for a user")
    void testGetEnabledMfaMethods() {
        // Given
        UUID userId = UUID.randomUUID();

        MfaConfiguration totpConfig = MfaConfiguration.create(userId, MfaMethod.TOTP, "TESTSECRET");
        totpConfig.markVerified();
        totpConfig.enable();

        MfaConfiguration smsConfig = MfaConfiguration.create(userId, MfaMethod.SMS, "+1234567890");
        smsConfig.markVerified();
        smsConfig.enable();

        when(mfaConfigRepository.findByUserIdAndEnabledTrue(userId))
                .thenReturn(List.of(totpConfig, smsConfig));

        // When
        List<MfaMethod> methods = mfaService.getEnabledMfaMethods(userId);

        // Then
        assertEquals(2, methods.size());
        assertTrue(methods.contains(MfaMethod.TOTP));
        assertTrue(methods.contains(MfaMethod.SMS));
    }

    @Test
    @DisplayName("Should clean up expired verification codes")
    void testCleanupExpiredCodes() {
        // Given
        LocalDateTime now = LocalDateTime.now();
        List<MfaVerificationCode> expiredCodes = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            MfaVerificationCode code = MfaVerificationCode.create(
                    UUID.randomUUID(), MfaMethod.SMS, "12345" + i, 5);
            ReflectionTestUtils.setField(code, "expiryDate", now.minusMinutes(10));
            expiredCodes.add(code);
        }

        when(verificationCodeRepository.findByUsedFalseAndExpiryDateBefore(any(LocalDateTime.class)))
                .thenReturn(expiredCodes);

        // When
        mfaService.cleanupExpiredCodes();

        // Then
        verify(verificationCodeRepository).saveAll(argThat(codes ->
                ((List<MfaVerificationCode>) codes).stream().allMatch(MfaVerificationCode::isUsed)));
    }
}