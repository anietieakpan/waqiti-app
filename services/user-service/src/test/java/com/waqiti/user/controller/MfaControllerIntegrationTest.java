/**
 * File: src/test/java/com/waqiti/user/controller/MfaControllerIntegrationTest.java
 * Integration tests for MFA Controller
 */
package com.waqiti.user.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waqiti.user.client.NotificationServiceClient;
import com.waqiti.user.domain.MfaConfiguration;
import com.waqiti.user.domain.MfaMethod;
import com.waqiti.user.domain.MfaVerificationCode;
import com.waqiti.user.domain.User;
import com.waqiti.user.dto.MfaSetupResponse;
import com.waqiti.user.dto.MfaStatusResponse;
import com.waqiti.user.repository.MfaConfigurationRepository;
import com.waqiti.user.repository.MfaVerificationCodeRepository;
import com.waqiti.user.repository.UserRepository;
import com.waqiti.user.security.JwtTokenProvider;
import dev.samstevens.totp.code.CodeGenerator;
import dev.samstevens.totp.code.DefaultCodeGenerator;
import dev.samstevens.totp.time.SystemTimeProvider;
import dev.samstevens.totp.time.TimeProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MfaControllerIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MfaConfigurationRepository mfaConfigRepository;

    @Autowired
    private MfaVerificationCodeRepository verificationCodeRepository;

    @MockBean
    private NotificationServiceClient notificationServiceClient;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtTokenProvider tokenProvider;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private String authToken;

    @BeforeEach
    void setup() {
        // Clean up the repositories
        mfaConfigRepository.deleteAll();
        verificationCodeRepository.deleteAll();

        // Create a test user
        String username = "mfatestuser";
        String password = "Password123!";
        String email = "mfatest@example.com";

        testUser = User.create(username, email, passwordEncoder.encode(password), "ext-123");
        testUser.activate(); // Activate the user for login
        testUser = userRepository.save(testUser);

        // Set up authentication token
        String token = tokenProvider.createAccessToken(
                testUser.getId(), username,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        authToken = "Bearer " + token;

        // Mock notification service
        when(notificationServiceClient.sendTwoFactorSms(any())).thenReturn(true);
        when(notificationServiceClient.sendTwoFactorEmail(any())).thenReturn(true);
    }

    @Test
    @DisplayName("Should return MFA status with no methods when not configured")
    void testGetMfaStatus_NoMfaConfigured() throws Exception {
        // When
        MvcResult result = mockMvc.perform(get("/api/v1/mfa/status")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andReturn();

        // Then
        MfaStatusResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                MfaStatusResponse.class
        );

        assertFalse(response.isEnabled());
        assertTrue(response.getMethods().isEmpty());
    }

    @Test
    @DisplayName("Should setup TOTP properly")
    void testSetupTotp() throws Exception {
        // When
        MvcResult result = mockMvc.perform(post("/api/v1/mfa/setup/totp")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON))
                .andReturn();

        // Then
        MfaSetupResponse response = objectMapper.readValue(
                result.getResponse().getContentAsString(),
                MfaSetupResponse.class
        );

        assertNotNull(response.getSecret());
        assertNotNull(response.getQrCodeImage());
        assertTrue(response.getQrCodeImage().startsWith("data:image/png;base64,"));

        // Verify config was created in database
        MfaConfiguration config = mfaConfigRepository.findByUserIdAndMethod(testUser.getId(), MfaMethod.TOTP)
                .orElse(null);
        assertNotNull(config);
        assertEquals(response.getSecret(), config.getSecret());
        assertFalse(config.isEnabled());  // Not verified yet
        assertFalse(config.isVerified());
    }

    @Test
    @DisplayName("Should setup and verify TOTP end-to-end")
    void testSetupAndVerifyTotp() throws Exception {
        // First setup TOTP
        MvcResult setupResult = mockMvc.perform(post("/api/v1/mfa/setup/totp")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andReturn();

        MfaSetupResponse setupResponse = objectMapper.readValue(
                setupResult.getResponse().getContentAsString(),
                MfaSetupResponse.class
        );

        String secret = setupResponse.getSecret();

        // Generate a TOTP code using the secret
        TimeProvider timeProvider = new SystemTimeProvider();
        CodeGenerator codeGenerator = new DefaultCodeGenerator();
        long counter = timeProvider.getTime() / 30;
        String validCode = codeGenerator.generate(secret, counter);

        // Verify the TOTP setup
        mockMvc.perform(post("/api/v1/mfa/verify/totp")
                        .header("Authorization", authToken)
                        .param("code", validCode))
                .andExpect(status().isOk());

        // Check MFA status
        MvcResult statusResult = mockMvc.perform(get("/api/v1/mfa/status")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andReturn();

        MfaStatusResponse statusResponse = objectMapper.readValue(
                statusResult.getResponse().getContentAsString(),
                MfaStatusResponse.class
        );

        assertTrue(statusResponse.isEnabled());
        assertEquals(1, statusResponse.getMethods().size());
        assertEquals(MfaMethod.TOTP, statusResponse.getMethods().get(0));

        // Verify in database
        MfaConfiguration config = mfaConfigRepository.findByUserIdAndMethod(testUser.getId(), MfaMethod.TOTP)
                .orElse(null);
        assertNotNull(config);
        assertTrue(config.isEnabled());
        assertTrue(config.isVerified());
    }

    @Test
    @DisplayName("Should setup SMS properly")
    void testSetupSms() throws Exception {
        // Given
        String phoneNumber = "+1234567890";

        // When
        mockMvc.perform(post("/api/v1/mfa/setup/sms")
                        .header("Authorization", authToken)
                        .param("phoneNumber", phoneNumber))
                .andExpect(status().isOk());

        // Then
        // Verify notification service was called
        verify(notificationServiceClient).sendTwoFactorSms(argThat(req ->
                req.getUserId().equals(testUser.getId()) && req.getRecipient().equals(phoneNumber)));

        // Verify config was created in database
        MfaConfiguration config = mfaConfigRepository.findByUserIdAndMethod(testUser.getId(), MfaMethod.SMS)
                .orElse(null);
        assertNotNull(config);
        assertEquals(phoneNumber, config.getSecret());
        assertFalse(config.isEnabled());  // Not verified yet
        assertFalse(config.isVerified());

        // Verify verification code was created
        Optional<MfaVerificationCode> code = verificationCodeRepository.findLatestActiveCode(
                testUser.getId(), MfaMethod.SMS, LocalDateTime.now());
        assertTrue(code.isPresent());
    }

    @Test
    @DisplayName("Should resend verification code successfully")
    void testResendVerificationCode() throws Exception {
        // First setup SMS
        String phoneNumber = "+1234567890";
        mockMvc.perform(post("/api/v1/mfa/setup/sms")
                        .header("Authorization", authToken)
                        .param("phoneNumber", phoneNumber))
                .andExpect(status().isOk());

        // Reset mock to verify next call
        reset(notificationServiceClient);
        when(notificationServiceClient.sendTwoFactorSms(any())).thenReturn(true);

        // Resend verification code
        mockMvc.perform(post("/api/v1/mfa/resend/SMS")
                        .header("Authorization", authToken))
                .andExpect(status().isOk());

        // Verify notification service was called again
        verify(notificationServiceClient).sendTwoFactorSms(argThat(req ->
                req.getUserId().equals(testUser.getId())));
    }

    @Test
    @DisplayName("Should verify SMS MFA setup properly")
    void testVerifySmsMfa() throws Exception {
        // First setup SMS
        String phoneNumber = "+1234567890";
        mockMvc.perform(post("/api/v1/mfa/setup/sms")
                        .header("Authorization", authToken)
                        .param("phoneNumber", phoneNumber))
                .andExpect(status().isOk());

        // Get the verification code
        MfaVerificationCode verificationCode = verificationCodeRepository.findLatestActiveCode(
                testUser.getId(), MfaMethod.SMS, LocalDateTime.now()).orElseThrow();

        // Verify the SMS setup
        mockMvc.perform(post("/api/v1/mfa/verify/SMS")
                        .header("Authorization", authToken)
                        .param("code", verificationCode.getCode()))
                .andExpect(status().isOk());

        // Check MFA status
        MvcResult statusResult = mockMvc.perform(get("/api/v1/mfa/status")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andReturn();

        MfaStatusResponse statusResponse = objectMapper.readValue(
                statusResult.getResponse().getContentAsString(),
                MfaStatusResponse.class
        );

        assertTrue(statusResponse.isEnabled());
        assertEquals(1, statusResponse.getMethods().size());
        assertEquals(MfaMethod.SMS, statusResponse.getMethods().get(0));
    }

    @Test
    @DisplayName("Should disable MFA method properly")
    void testDisableMfaMethod() throws Exception {
        // First setup and verify TOTP
        testSetupAndVerifyTotp();

        // Disable TOTP
        mockMvc.perform(post("/api/v1/mfa/disable/TOTP")
                        .header("Authorization", authToken))
                .andExpect(status().isOk());

        // Check MFA status
        MvcResult statusResult = mockMvc.perform(get("/api/v1/mfa/status")
                        .header("Authorization", authToken))
                .andExpect(status().isOk())
                .andReturn();

        MfaStatusResponse statusResponse = objectMapper.readValue(
                statusResult.getResponse().getContentAsString(),
                MfaStatusResponse.class
        );

        assertFalse(statusResponse.isEnabled());
        assertTrue(statusResponse.getMethods().isEmpty());

        // Verify in database
        MfaConfiguration config = mfaConfigRepository.findByUserIdAndMethod(testUser.getId(), MfaMethod.TOTP)
                .orElse(null);
        assertNotNull(config);
        assertFalse(config.isEnabled());
    }
}