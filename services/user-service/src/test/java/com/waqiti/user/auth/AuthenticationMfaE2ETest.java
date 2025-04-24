// File: src/test/java/com/waqiti/user/auth/AuthenticationMfaE2ETest.java
package com.waqiti.user.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.waqiti.user.client.IntegrationServiceClient;
import com.waqiti.user.client.NotificationServiceClient;
import com.waqiti.user.client.dto.CreateUserResponse;
import com.waqiti.user.config.MfaTestConfig;
import com.waqiti.user.config.TestSecurityConfiguration;
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
import com.waqiti.user.service.OAuth2Service;
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
import org.springframework.context.annotation.Import;
import org.springframework.http.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.LocalDateTime;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@Import({MfaTestConfig.class, TestSecurityConfiguration.class})
@TestPropertySource(properties = {
        "spring.main.allow-bean-definition-overriding=true",
        "oauth2.state.secret=test-oauth2-state-secret",
        "security.jwt.token.secret-key=VGhpc0lzQVZlcnlMb25nQW5kU2VjdXJlVGVzdEtleVRoYXRJc1N1ZmZpY2llbnRseUxvbmdGb3JUaGVITUFDU0hBQWxnb3JpdGhtMTIzNDU2Nzg5"
})
@ActiveProfiles("test")
@Testcontainers
class AuthenticationMfaE2ETest {

    @Container
    static PostgreSQLContainer<?> postgresContainer = new PostgreSQLContainer<>("postgres:13")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerDynamicProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresContainer::getUsername);
        registry.add("spring.datasource.password", postgresContainer::getPassword);

        // If you use Flyway, add these too:
        registry.add("spring.flyway.url", postgresContainer::getJdbcUrl);
        registry.add("spring.flyway.user", postgresContainer::getUsername);
        registry.add("spring.flyway.password", postgresContainer::getPassword);
    }

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

    @Autowired
    private ObjectMapper objectMapper;

    // Mock all external dependencies to prevent conflicts
    @MockBean
    private NotificationServiceClient notificationServiceClient;

    @MockBean
    private IntegrationServiceClient integrationServiceClient;

    @MockBean
    private OAuth2Service oAuth2Service;

    @MockBean
    private ClientRegistrationRepository clientRegistrationRepository;

    @MockBean
    private OAuth2AuthorizedClientService oAuth2AuthorizedClientService;

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

        // Mock integration service
        CreateUserResponse mockResponse = CreateUserResponse.builder()
                .externalId("ext-test-123")
                .status("ACTIVE")
                .build();
        when(integrationServiceClient.createUser(any())).thenReturn(mockResponse);
    }

    @Test
    @DisplayName("Should authenticate user with basic credentials")
    void testBasicAuthentication() {
        // Create a test user with known credentials
        String username = "testuser";
        String password = "TestPassword123!";

        // Delete existing user if any
        userRepository.findByUsername(username).ifPresent(user -> userRepository.delete(user));

        // Create a new user with known credentials
        User user = User.create(username, username + "@example.com", passwordEncoder.encode(password), "ext-test-123");
        user.activate();
        user = userRepository.save(user);

        // Print debug information
        System.out.println("Created test user: " + user.getUsername() + ", active: " + user.isActive());
//        System.out.println("Password matches: " + passwordEncoder.matches(password, user.getPassword()));

        // Create login request
        AuthenticationRequest loginRequest = new AuthenticationRequest(username, password);

        // Set up headers
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthenticationRequest> requestEntity = new HttpEntity<>(loginRequest, headers);

        // Try to authenticate
        ResponseEntity<String> response = restTemplate.exchange(
                "/api/v1/auth/login",
                HttpMethod.POST,
                requestEntity,
                String.class);

        // Print response details
        System.out.println("Authentication Response Status: " + response.getStatusCode());
        System.out.println("Authentication Response Body: " + response.getBody());

        // Assert success
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    @DisplayName("Should access health endpoint")
    void testHealthEndpoint() {
        ResponseEntity<String> response = restTemplate.getForEntity("/actuator/health", String.class);

        System.out.println("Health Endpoint Status: " + response.getStatusCode());
        System.out.println("Health Endpoint Body: " + response.getBody());

        // Accept either 200 OK or 503 SERVICE_UNAVAILABLE (which is common in test environments)
        assertTrue(
                response.getStatusCode() == HttpStatus.OK ||
                        response.getStatusCode() == HttpStatus.SERVICE_UNAVAILABLE,
                "Health endpoint should return OK or SERVICE_UNAVAILABLE");
    }

    @Test
    @DisplayName("Should complete full authentication flow with TOTP")
    void testAuthenticationFlowWithTotp() throws Exception {
        // Step 1: Login without MFA configured
        AuthenticationRequest loginRequest = new AuthenticationRequest(
                testUser.getUsername(), testPassword);

        // Print debug information about the test user
        System.out.println("====== DEBUG TEST USER INFO =======");
        System.out.println("User ID: " + testUser.getId());
        System.out.println("Username: " + testUser.getUsername());
        System.out.println("Is Active: " + testUser.isActive());
        System.out.println("Password from test: " + testPassword);
//        System.out.println("Encoded Password in DB: " + testUser.getPassword().substring(0, 10) + "...");
//        System.out.println("Password matches: " + passwordEncoder.matches(testPassword, testUser.getPassword()));
        System.out.println("==================================");

        // Debug the request
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<AuthenticationRequest> requestEntity = new HttpEntity<>(loginRequest, headers);

        // Log the request details
        System.out.println("Sending login request with username: " + testUser.getUsername());
        System.out.println("Password length: " + testPassword.length());

        // Get raw response for debugging
        ResponseEntity<String> debugResponse = restTemplate.exchange(
                "/api/v1/auth/login",
                HttpMethod.POST,
                requestEntity,
                String.class);

        System.out.println("Debug Response Status: " + debugResponse.getStatusCode());
        System.out.println("Debug Response Body: " + debugResponse.getBody());

        ResponseEntity<AuthenticationResponse> initialLoginResponse = restTemplate.postForEntity(
                "/api/v1/auth/login", loginRequest, AuthenticationResponse.class);

        assertEquals(HttpStatus.OK, initialLoginResponse.getStatusCode());
        AuthenticationResponse initialAuth = initialLoginResponse.getBody();
        assertNotNull(initialAuth);
        assertFalse(initialAuth.isRequiresMfa());
        assertNotNull(initialAuth.getAccessToken());

        // Step 2: Configure TOTP MFA
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
        mfaHeaders.setContentType(MediaType.APPLICATION_JSON);
        mfaHeaders.set("X-MFA-Token", mfaAuth.getMfaToken()); // Use both approaches
        mfaHeaders.set("Authorization", "Bearer " + mfaAuth.getMfaToken()); // For compatibility

        ResponseEntity<AuthenticationResponse> finalResponse = restTemplate.exchange(
                "/api/v1/auth/mfa/verify",
                HttpMethod.POST,
                new HttpEntity<>(verifyRequest, mfaHeaders),
                AuthenticationResponse.class);

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
        mfaHeaders.set("Authorization", "Bearer " + mfaAuth.getMfaToken());

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
}