/**
 * File: src/test/java/com/waqiti/user/validation/MfaValidationTest.java
 * Input validation tests for MFA functionality
 */
package com.waqiti.user.validation;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waqiti.user.domain.User;
import com.waqiti.user.repository.UserRepository;
import com.waqiti.user.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MfaValidationTest {
    @Autowired
    private MockMvc mockMvc;

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
        // Create a test user
        String username = "validationuser";
        String password = "Password123!";
        String email = "validation@example.com";

        testUser = User.create(username, email, passwordEncoder.encode(password), "ext-valid-123");
        testUser.activate();
        testUser = userRepository.save(testUser);

        // Set up authentication token
        String token = tokenProvider.createAccessToken(
                testUser.getId(), username,
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        authToken = "Bearer " + token;
    }

    @Test
    @DisplayName("Should reject invalid phone number format")
    void testInvalidPhoneNumberFormat() throws Exception {
        // Test with invalid phone number
        mockMvc.perform(post("/api/v1/mfa/setup/sms")
                        .header("Authorization", authToken)
                        .param("phoneNumber", "123456789")) // Missing + and country code
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        // Test with too short phone number
        mockMvc.perform(post("/api/v1/mfa/setup/sms")
                        .header("Authorization", authToken)
                        .param("phoneNumber", "+123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        // Test with non-numeric characters
        mockMvc.perform(post("/api/v1/mfa/setup/sms")
                        .header("Authorization", authToken)
                        .param("phoneNumber", "+1234abcde"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should reject invalid email format")
    void testInvalidEmailFormat() throws Exception {
        // Test with invalid email
        mockMvc.perform(post("/api/v1/mfa/setup/email")
                        .header("Authorization", authToken)
                        .param("email", "not-an-email"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        // Test with missing @
        mockMvc.perform(post("/api/v1/mfa/setup/email")
                        .header("Authorization", authToken)
                        .param("email", "userdomain.com"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        // Test with missing domain
        mockMvc.perform(post("/api/v1/mfa/setup/email")
                        .header("Authorization", authToken)
                        .param("email", "user@"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }

    @Test
    @DisplayName("Should reject invalid TOTP code format")
    void testInvalidTotpCodeFormat() throws Exception {
        // Test with too short code
        mockMvc.perform(post("/api/v1/mfa/verify/totp")
                        .header("Authorization", authToken)
                        .param("code", "123"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        // Test with non-numeric code
        mockMvc.perform(post("/api/v1/mfa/verify/totp")
                        .header("Authorization", authToken)
                        .param("code", "abcdef"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());

        // Test with too long code
        mockMvc.perform(post("/api/v1/mfa/verify/totp")
                        .header("Authorization", authToken)
                        .param("code", "12345678"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").exists());
    }
}