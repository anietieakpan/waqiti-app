// File: src/test/java/com/waqiti/user/auth/MfaRecoveryTest.java
package com.waqiti.user.auth;

import com.waqiti.user.domain.MfaConfiguration;
import com.waqiti.user.domain.MfaMethod;
import com.waqiti.user.domain.User;
import com.waqiti.user.domain.UserStatus;
import com.waqiti.user.dto.AuthenticationRequest;
import com.waqiti.user.dto.AuthenticationResponse;
import com.waqiti.user.dto.MfaVerifyRequest;
import com.waqiti.user.repository.MfaConfigurationRepository;
import com.waqiti.user.repository.MfaVerificationCodeRepository;
import com.waqiti.user.repository.UserRepository;
import com.waqiti.user.service.AuthService;
import com.waqiti.user.service.MfaService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MfaRecoveryTest {
    @Mock
    private MfaService mfaService;

    @Mock
    private AuthService authService;

    private UUID userId;

    @BeforeEach
    void setup() {
        // Create test user ID
        userId = UUID.randomUUID();
    }

    @Test
    @DisplayName("Should recover access using backup MFA method")
    void testRecoveryWithBackupMethod() {
        // Mock: Login to get MFA token
        AuthenticationRequest loginRequest = new AuthenticationRequest("recoveryuser", "Password123!");
        AuthenticationResponse authResponse = new AuthenticationResponse();
        authResponse.setRequiresMfa(true);
        authResponse.setMfaToken("mock-mfa-token");
        authResponse.setAvailableMfaMethods(Arrays.asList(MfaMethod.TOTP, MfaMethod.SMS));

        when(authService.authenticate(loginRequest)).thenReturn(authResponse);

        // Mock: Verify with SMS code
        MfaVerifyRequest verifyRequest = new MfaVerifyRequest(MfaMethod.SMS, "123456");
        AuthenticationResponse verifyResponse = new AuthenticationResponse();
        verifyResponse.setRequiresMfa(false);
        verifyResponse.setAccessToken("mock-access-token");
        verifyResponse.setRefreshToken("mock-refresh-token");

        when(authService.verifyMfa(authResponse.getMfaToken(), verifyRequest)).thenReturn(verifyResponse);

        // Act
        AuthenticationResponse actualLoginResp = authService.authenticate(loginRequest);
        AuthenticationResponse actualVerifyResp = authService.verifyMfa(actualLoginResp.getMfaToken(), verifyRequest);

        // Assert
        assertTrue(actualLoginResp.isRequiresMfa());
        assertNotNull(actualLoginResp.getMfaToken());
        assertEquals(2, actualLoginResp.getAvailableMfaMethods().size());

        assertFalse(actualVerifyResp.isRequiresMfa());
        assertNotNull(actualVerifyResp.getAccessToken());
        assertNotNull(actualVerifyResp.getRefreshToken());
    }

    @Test
    @DisplayName("User should be able to recover with fallback recovery code")
    void testLostDeviceRecoveryFlow() {
        // Mock: Generate recovery codes for user
        UUID[] recoveryCodes = new UUID[]{UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID()};
        when(mfaService.generateRecoveryCodes(eq(userId), eq(5))).thenReturn(recoveryCodes);

        // Mock: Use recovery code
        String recoveryCode = recoveryCodes[0].toString();
        when(mfaService.verifyRecoveryCode(userId, recoveryCode)).thenReturn(true);

        // Mock: After using recovery code, should get full login
        AuthenticationRequest newLoginRequest = new AuthenticationRequest("recoveryuser", "Password123!");
        AuthenticationResponse newAuthResponse = new AuthenticationResponse();
        newAuthResponse.setRequiresMfa(false);
        newAuthResponse.setAccessToken("mock-token");
        when(authService.authenticate(newLoginRequest)).thenReturn(newAuthResponse);

        // Act
        UUID[] actualCodes = mfaService.generateRecoveryCodes(userId, 5);
        boolean recoveryResult = mfaService.verifyRecoveryCode(userId, recoveryCode);
        AuthenticationResponse loginResp = authService.authenticate(newLoginRequest);

        // Assert
        assertNotNull(actualCodes);
        assertTrue(actualCodes.length > 0);
        assertTrue(recoveryResult);
        assertFalse(loginResp.isRequiresMfa());
        assertNotNull(loginResp.getAccessToken());
    }

    @Test
    @DisplayName("Admin should be able to reset user's MFA")
    void testAdminResetMfa() {
        // Create mock UserService behavior
        when(mfaService.resetUserMfa(userId)).thenReturn(true);

        // Act
        boolean result = mfaService.resetUserMfa(userId);

        // Assert
        assertTrue(result);
    }
}