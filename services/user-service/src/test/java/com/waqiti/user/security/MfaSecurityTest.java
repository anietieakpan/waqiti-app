/**
 * File: src/test/java/com/waqiti/user/security/MfaSecurityTest.java
 * Security tests for MFA implementation
 */
package com.waqiti.user.security;

import com.waqiti.user.domain.AuthenticationFailedException;
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
import org.springframework.security.crypto.password.PasswordEncoder;

//added to address TestPropertyExtensions error
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.DynamicPropertySource;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@TestPropertySource(properties = {
        "security.jwt.token.access-token-expire-length=5000", // 5 seconds for testing
        "security.jwt.token.mfa-token-expire-length=3000"     // 3 seconds for testing
})
class MfaSecurityTest {
    @Autowired
    private AuthService authService;

    @Autowired
    private MfaService mfaService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MfaConfigurationRepository mfaConfigRepository;

    @Autowired
    private MfaVerificationCodeRepository verificationCodeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User testUser;

    @BeforeEach
    void setup() {
        // Clean repositories
        mfaConfigRepository.deleteAll();
        verificationCodeRepository.deleteAll();
        userRepository.deleteAll();

        // Create test user
        testUser = User.create("securityuser", "security@example.com",
                passwordEncoder.encode("Password123!"), "ext-sec-123");
        testUser.activate();
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("MFA token should expire after configured time")
    void testMfaTokenExpiry() throws Exception {
        // Setup TOTP
        setupTotp();

        // Get MFA token
        AuthenticationRequest request = new AuthenticationRequest("securityuser", "Password123!");
        AuthenticationResponse response = authService.authenticate(request);

        assertTrue(response.isRequiresMfa());
        String mfaToken = response.getMfaToken();

        // Wait for token to expire (3 seconds)
        Thread.sleep(3500);

        // Try to verify MFA with expired token
        MfaVerifyRequest verifyRequest = new MfaVerifyRequest(MfaMethod.TOTP, "123456");

        assertThrows(AuthenticationFailedException.class, () ->
                authService.verifyMfa(mfaToken, verifyRequest));
    }

    @Test
    @DisplayName("Verification code should not be reusable")
    void testVerificationCodeReuseProtection() {
        // Setup SMS MFA
        MfaConfiguration smsConfig = MfaConfiguration.create(
                testUser.getId(), MfaMethod.SMS, "+1234567890");
        smsConfig.markVerified();
        smsConfig.enable();
        mfaConfigRepository.save(smsConfig);

        // Create verification code
        MfaVerificationCode verificationCode = MfaVerificationCode.create(
                testUser.getId(), MfaMethod.SMS, "123456", 5);
        verificationCodeRepository.save(verificationCode);

        // First verification should succeed
        boolean firstResult = mfaService.verifyMfaCode(
                testUser.getId(), MfaMethod.SMS, "123456");
        assertTrue(firstResult);

        // Second verification with same code should fail
        boolean secondResult = mfaService.verifyMfaCode(
                testUser.getId(), MfaMethod.SMS, "123456");
        assertFalse(secondResult);
    }

    @Test
    @DisplayName("Verification code should expire after time limit")
    void testVerificationCodeExpiry() throws Exception {
        // Setup SMS MFA
        MfaConfiguration smsConfig = MfaConfiguration.create(
                testUser.getId(), MfaMethod.SMS, "+1234567890");
        smsConfig.markVerified();
        smsConfig.enable();
        mfaConfigRepository.save(smsConfig);

        // Create verification code that expires in 1 second
        MfaVerificationCode verificationCode = MfaVerificationCode.create(
                testUser.getId(), MfaMethod.SMS, "123456", (int) (1/60.0f)); // 1 second
        verificationCodeRepository.save(verificationCode);

        // Wait for code to expire
        Thread.sleep(1500);

        // Verification should fail with expired code
        boolean result = mfaService.verifyMfaCode(
                testUser.getId(), MfaMethod.SMS, "123456");
        assertFalse(result);
    }

    private void setupTotp() {
        MfaConfiguration totpConfig = MfaConfiguration.create(
                testUser.getId(), MfaMethod.TOTP, "ABCDEFGHIJKLMNOP");
        totpConfig.markVerified();
        totpConfig.enable();
        mfaConfigRepository.save(totpConfig);
    }
}