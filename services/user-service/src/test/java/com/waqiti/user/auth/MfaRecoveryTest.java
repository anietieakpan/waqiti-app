/**
 * File: src/test/java/com/waqiti/user/auth/MfaRecoveryTest.java
 * Tests for MFA recovery scenarios
 */
package com.waqiti.user.auth;


import com.waqiti.user.config.TestOAuth2Config;
import com.waqiti.user.domain.MfaConfiguration;
import com.waqiti.user.domain.MfaMethod;
import com.waqiti.user.domain.MfaVerificationCode;
import com.waqiti.user.domain.User;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;
import org.springframework.boot.test.web.client.TestRestTemplate;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Import(TestOAuth2Config.class)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:tc:postgresql:13:///testdb",
        "security.jwt.token.secret-key=dGVzdHNlY3JldGtleWZvcnVuaXR0ZXN0c29ubHlub3Rmb3Jwcm9kdWN0aW9udGVzdHNlY3JldGtleWZvcnVuaXR0ZXN0cw==",
        "security.jwt.token.access-token-expire-length=3600000",
        "security.jwt.token.refresh-token-expire-length=86400000",
        "security.jwt.token.mfa-token-expire-length=300000"

})
class MfaRecoveryTest {
    @Autowired
    private MfaService mfaService;

    @Autowired
    private AuthService authService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MfaConfigurationRepository mfaConfigRepository;

    @Autowired
    private MfaVerificationCodeRepository verificationCodeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private TestRestTemplate restTemplate;

    private User testUser;

    @BeforeEach
    void setup() {
        // Clean repositories
        mfaConfigRepository.deleteAll();
        verificationCodeRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = User.create("recoveryuser", "recovery@example.com",
                passwordEncoder.encode("Password123!"), "ext-rec-123");
        testUser.activate();
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("Should recover access using backup MFA method")
    void testRecoveryWithBackupMethod() {
        // Setup both TOTP and SMS methods
        setupTotpAndSms();

        // Login to get MFA token
        AuthenticationRequest loginRequest = new AuthenticationRequest("recoveryuser", "Password123!");
        AuthenticationResponse authResponse = authService.authenticate(loginRequest);

        assertTrue(authResponse.isRequiresMfa());
        assertNotNull(authResponse.getMfaToken());
        assertEquals(2, authResponse.getAvailableMfaMethods().size());

        // Simulate TOTP device loss by using SMS instead
        MfaVerificationCode smsCode = MfaVerificationCode.create(
                testUser.getId(), MfaMethod.SMS, "123456", 5);
        verificationCodeRepository.save(smsCode);

        // Verify with SMS code
        MfaVerifyRequest verifyRequest = new MfaVerifyRequest(MfaMethod.SMS, smsCode.getCode());
        AuthenticationResponse verifyResponse = authService.verifyMfa(authResponse.getMfaToken(), verifyRequest);

        // Should successfully authenticate
        assertFalse(verifyResponse.isRequiresMfa());
        assertNotNull(verifyResponse.getAccessToken());
        assertNotNull(verifyResponse.getRefreshToken());
    }

    @Test
    @DisplayName("Admin should be able to reset user's MFA")
    void testAdminResetMfa() {
        // Setup TOTP
        setupTotp();

        // Create admin user
        User adminUser = User.create("adminuser", "admin@example.com",
                passwordEncoder.encode("AdminPass123!"), "ext-admin-123");
        adminUser.activate();
        adminUser.addRole("ROLE_ADMIN");
        adminUser = userRepository.save(adminUser);

        // Get admin token
        AuthenticationRequest adminLoginRequest = new AuthenticationRequest("adminuser", "AdminPass123!");
        AuthenticationResponse adminAuthResponse = authService.authenticate(adminLoginRequest);

        // Call admin endpoint to reset MFA
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(adminAuthResponse.getAccessToken());

        ResponseEntity<Void> resetResponse = restTemplate.exchange(
                "/api/v1/admin/users/" + testUser.getId() + "/mfa/reset",
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(200, resetResponse.getStatusCodeValue());

        // Verify MFA was removed
        assertTrue(mfaConfigRepository.findByUserIdAndMethod(testUser.getId(), MfaMethod.TOTP).isEmpty());
    }

    @Test
    @DisplayName("User should be able to recover with fallback recovery code")
    void testLostDeviceRecoveryFlow() {
        // Setup TOTP
        setupTotp();

        // Generate recovery codes for user
        UUID[] recoveryCodes = mfaService.generateRecoveryCodes(testUser.getId(), 5);
        assertNotNull(recoveryCodes);
        assertTrue(recoveryCodes.length > 0);

        // Login to get MFA token
        AuthenticationRequest loginRequest = new AuthenticationRequest("recoveryuser", "Password123!");
        AuthenticationResponse authResponse = authService.authenticate(loginRequest);

        assertTrue(authResponse.isRequiresMfa());

        // Use recovery code instead of TOTP
        String recoveryCode = recoveryCodes[0].toString();
        boolean recoveryResult = mfaService.verifyRecoveryCode(testUser.getId(), recoveryCode);

        assertTrue(recoveryResult);

        // After using recovery code, should get full login
        AuthenticationRequest newLoginRequest = new AuthenticationRequest("recoveryuser", "Password123!");
        AuthenticationResponse newAuthResponse = authService.authenticate(newLoginRequest);

        // MFA should be disabled after recovery
        assertFalse(newAuthResponse.isRequiresMfa());
        assertNotNull(newAuthResponse.getAccessToken());
    }

    private void setupTotp() {
        MfaConfiguration totpConfig = MfaConfiguration.create(
                testUser.getId(), MfaMethod.TOTP, "ABCDEFGHIJKLMNOP");
        totpConfig.markVerified();
        totpConfig.enable();
        mfaConfigRepository.save(totpConfig);
    }

    private void setupSms() {
        MfaConfiguration smsConfig = MfaConfiguration.create(
                testUser.getId(), MfaMethod.SMS, "+1234567890");
        smsConfig.markVerified();
        smsConfig.enable();
        mfaConfigRepository.save(smsConfig);
    }

    private void setupTotpAndSms() {
        setupTotp();
        setupSms();
    }
}