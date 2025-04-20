/**
 * File: src/test/java/com/waqiti/user/auth/AuthenticationMfaE2ETest.java
 * End-to-end tests for the complete MFA authentication flow
 */
package com.waqiti.user.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waqiti.user.client.NotificationServiceClient;
import com.waqiti.user.domain.MfaConfiguration;
import com.waqiti.user.domain.MfaMethod;
import com.waqiti.user.domain.MfaVerificationCode;
import com.waqiti.user.domain.User;
import com.waqiti.user.dto.AuthenticationRequest;
import com.waqiti.user.dto.AuthenticationResponse;
import com.waqiti.user.dto.MfaSetupResponse;
import com.waqiti.user.dto.MfaVerifyRequest;
import com.waqiti.user.repository.MfaConfigurationRepository;
import com.waqiti.user.repository.MfaVerificationCodeRepository;
import com.waqiti.user.repository.UserRepository;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.exceptions.CodeGenerationException;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.TestPropertySource;


import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:tc:postgresql:13:///testdb",
        "spring.kafka.bootstrap-servers=${spring.embedded.kafka.brokers}"
})
class AuthenticationMfaE2ETest {
    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MfaConfigurationRepository mfaConfigRepository;

    @Autowired
    private MfaVerificationCodeRepository verificationCodeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private NotificationServiceClient notificationServiceClient;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private final String testPassword = "Password123!";

    @BeforeEach
    void setup() {
        // Clean repositories
        mfaConfigRepository.deleteAll();
        verificationCodeRepository.deleteAll();
        userRepository.deleteAll();

        // Create a test user
        String username = "authuser";
        String email = "authuser@example.com";

        testUser = User.create(username, email, passwordEncoder.encode(testPassword), "ext-auth-123");
        testUser.activate(); // Activate the user
        testUser = userRepository.save(testUser);

        // Mock notification service
        when(notificationServiceClient.sendTwoFactorSms(any())).thenReturn(true);
        when(notificationServiceClient.sendTwoFactorEmail(any())).thenReturn(true);
    }

    @Test
    @DisplayName("Should complete full authentication flow with TOTP")
    void testAuthenticationFlowWithTotp() throws Exception {
        // Step 1: Login without MFA configured
        AuthenticationRequest loginRequest = new AuthenticationRequest(
                testUser.getUsername(), testPassword);

        ResponseEntity<AuthenticationResponse> initialLoginResponse = restTemplate.postForEntity(
                "/api/v1/auth/login", loginRequest, AuthenticationResponse.class);

        assertEquals(HttpStatus.OK, initialLoginResponse.getStatusCode());
        AuthenticationResponse initialAuth = initialLoginResponse.getBody();
        assertNotNull(initialAuth);
        assertFalse(initialAuth.isRequiresMfa());
        assertNotNull(initialAuth.getAccessToken());

        // Step 2: Configure TOTP MFA
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(initialAuth.getAccessToken());

        ResponseEntity<MfaSetupResponse> totpSetupResponse = restTemplate.exchange(
                "/api/v1/mfa/setup/totp", HttpMethod.POST,
                new HttpEntity<>(headers), MfaSetupResponse.class);

        assertEquals(HttpStatus.OK, totpSetupResponse.getStatusCode());
        MfaSetupResponse setupResponse = totpSetupResponse.getBody();
        assertNotNull(setupResponse);
        String totpSecret = setupResponse.getSecret();

        // Step 3: Generate a valid TOTP code and verify setup
        String validCode = generateTotpCode(totpSecret);

        ResponseEntity<Void> verifyResponse = restTemplate.exchange(
                "/api/v1/mfa/verify/totp?code=" + validCode,
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.OK, verifyResponse.getStatusCode());

        // Step 4: Try to login again (now requiring MFA)
        ResponseEntity<AuthenticationResponse> mfaLoginResponse = restTemplate.postForEntity(
                "/api/v1/auth/login", loginRequest, AuthenticationResponse.class);

        assertEquals(HttpStatus.OK, mfaLoginResponse.getStatusCode());
        AuthenticationResponse mfaAuth = mfaLoginResponse.getBody();
        assertNotNull(mfaAuth);
        assertTrue(mfaAuth.isRequiresMfa());
        assertNotNull(mfaAuth.getMfaToken());
        assertNull(mfaAuth.getAccessToken());
        assertEquals(Collections.singletonList(MfaMethod.TOTP), mfaAuth.getAvailableMfaMethods());

        // Step 5: Generate a fresh TOTP code and complete MFA verification
        String freshCode = generateTotpCode(totpSecret);
        MfaVerifyRequest verifyRequest = new MfaVerifyRequest(MfaMethod.TOTP, freshCode);

        HttpHeaders mfaHeaders = new HttpHeaders();
        mfaHeaders.set("X-MFA-Token", mfaAuth.getMfaToken());
        mfaHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<AuthenticationResponse> finalResponse = restTemplate.exchange(
                "/api/v1/auth/mfa/verify", HttpMethod.POST,
                new HttpEntity<>(verifyRequest, mfaHeaders), AuthenticationResponse.class);

        // Step 6: Verify successful authentication with full tokens
        assertEquals(HttpStatus.OK, finalResponse.getStatusCode());
        AuthenticationResponse completedAuth = finalResponse.getBody();
        assertNotNull(completedAuth);
        assertFalse(completedAuth.isRequiresMfa());
        assertNotNull(completedAuth.getAccessToken());
        assertNotNull(completedAuth.getRefreshToken());
    }

    @Test
    @DisplayName("Should complete full authentication flow with SMS")
    void testAuthenticationFlowWithSms() throws Exception {
        // Step 1: Login without MFA configured
        AuthenticationRequest loginRequest = new AuthenticationRequest(
                testUser.getUsername(), testPassword);

        ResponseEntity<AuthenticationResponse> initialLoginResponse = restTemplate.postForEntity(
                "/api/v1/auth/login", loginRequest, AuthenticationResponse.class);

        assertEquals(HttpStatus.OK, initialLoginResponse.getStatusCode());
        AuthenticationResponse initialAuth = initialLoginResponse.getBody();
        assertNotNull(initialAuth);
        String accessToken = initialAuth.getAccessToken();

        // Step 2: Configure SMS MFA
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);

        String phoneNumber = "+1234567890";
        ResponseEntity<Boolean> smsSetupResponse = restTemplate.exchange(
                "/api/v1/mfa/setup/sms?phoneNumber=" + phoneNumber,
                HttpMethod.POST, new HttpEntity<>(headers), Boolean.class);

        assertEquals(HttpStatus.OK, smsSetupResponse.getStatusCode());

        // Step 3: Get the verification code from the database and verify setup
        MfaVerificationCode verificationCode = verificationCodeRepository.findLatestActiveCode(
                testUser.getId(), MfaMethod.SMS, LocalDateTime.now()).orElseThrow();

        ResponseEntity<Void> verifyResponse = restTemplate.exchange(
                "/api/v1/mfa/verify/SMS?code=" + verificationCode.getCode(),
                HttpMethod.POST, new HttpEntity<>(headers), Void.class);

        assertEquals(HttpStatus.OK, verifyResponse.getStatusCode());

        // Step 4: Try to login again (now requiring MFA)
        ResponseEntity<AuthenticationResponse> mfaLoginResponse = restTemplate.postForEntity(
                "/api/v1/auth/login", loginRequest, AuthenticationResponse.class);

        assertEquals(HttpStatus.OK, mfaLoginResponse.getStatusCode());
        AuthenticationResponse mfaAuth = mfaLoginResponse.getBody();
        assertNotNull(mfaAuth);
        assertTrue(mfaAuth.isRequiresMfa());
        assertNotNull(mfaAuth.getMfaToken());
        assertEquals(MfaMethod.SMS, mfaAuth.getAvailableMfaMethods().get(0));

        // Step 5: Simulate receiving a new SMS code
        MfaVerificationCode loginCode = MfaVerificationCode.create(
                testUser.getId(), MfaMethod.SMS, "654321", 5);
        verificationCodeRepository.save(loginCode);

        // Step 6: Complete MFA verification with SMS code
        MfaVerifyRequest verifyRequest = new MfaVerifyRequest(MfaMethod.SMS, loginCode.getCode());

        HttpHeaders mfaHeaders = new HttpHeaders();
        mfaHeaders.set("X-MFA-Token", mfaAuth.getMfaToken());
        mfaHeaders.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<AuthenticationResponse> finalResponse = restTemplate.exchange(
                "/api/v1/auth/mfa/verify", HttpMethod.POST,
                new HttpEntity<>(verifyRequest, mfaHeaders), AuthenticationResponse.class);

        // Step 7: Verify successful authentication
        assertEquals(HttpStatus.OK, finalResponse.getStatusCode());
        AuthenticationResponse completedAuth = finalResponse.getBody();
        assertNotNull(completedAuth);
        assertFalse(completedAuth.isRequiresMfa());
        assertNotNull(completedAuth.getAccessToken());
        assertNotNull(completedAuth.getRefreshToken());
    }

    // Helper methods

    private String generateTotpCode(String secret) throws CodeGenerationException {
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        long counter = timeProvider.getTime() / 30;
        return codeGenerator.generate(secret, counter);
    }

    private void setupTotp() {
        // Create TOTP configuration
        MfaConfiguration totpConfig = MfaConfiguration.create(
                testUser.getId(), MfaMethod.TOTP, "ORZXG2LQOJUXIZLTOQXQ====");
        totpConfig.markVerified();
        totpConfig.enable();
        mfaConfigRepository.save(totpConfig);
    }

    private void setupSms() {
        // Create SMS configuration
        MfaConfiguration smsConfig = MfaConfiguration.create(
                testUser.getId(), MfaMethod.SMS, "+1234567890");
        smsConfig.markVerified();
        smsConfig.enable();
        mfaConfigRepository.save(smsConfig);
    }
}