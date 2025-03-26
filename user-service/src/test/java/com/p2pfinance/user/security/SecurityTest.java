// File: src/test/java/com/p2pfinance/user/security/SecurityTest.java
package com.p2pfinance.user.security;

import com.p2pfinance.user.api.OAuth2Controller;
import com.p2pfinance.user.client.IntegrationServiceClient;
import com.p2pfinance.user.client.dto.CreateUserResponse;
import com.p2pfinance.user.config.TestJwtSecurityConfig;
import com.p2pfinance.user.config.TestSecurityConfig;
import com.p2pfinance.user.domain.User;
import com.p2pfinance.user.dto.AuthenticationRequest;
import com.p2pfinance.user.dto.AuthenticationResponse;
import com.p2pfinance.user.dto.UserResponse;
import com.p2pfinance.user.repository.UserRepository;
import com.p2pfinance.user.service.AuthService;
import com.p2pfinance.user.service.OAuth2Service;
import com.p2pfinance.user.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.AutoConfigureDataJpa;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(
        properties = {
                "spring.main.allow-bean-definition-overriding=true",
                "spring.flyway.enabled=false",
                "spring.security.oauth2.resourceserver.jwt.enabled=false"
        },
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@AutoConfigureMockMvc
@AutoConfigureDataJpa
@ActiveProfiles("test")
@Testcontainers
@Import({TestSecurityConfig.class, TestJwtSecurityConfig.class})
@Transactional
@Slf4j
public class SecurityTest {

    @Container
    static PostgreSQLContainer<?> postgresqlContainer = new PostgreSQLContainer<>("postgres:16")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void registerPgProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgresqlContainer::getJdbcUrl);
        registry.add("spring.datasource.username", postgresqlContainer::getUsername);
        registry.add("spring.datasource.password", postgresqlContainer::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");

        // OAuth2 client registration with concrete values
        registry.add("spring.security.oauth2.client.registration.google.client-id", () -> "test-client-id");
        registry.add("spring.security.oauth2.client.registration.google.client-secret", () -> "test-client-secret");

        // Disable circuit breaker for tests
        registry.add("resilience4j.circuitbreaker.instances.integrationService.failureRateThreshold", () -> "100");
        registry.add("resilience4j.circuitbreaker.instances.integrationService.slidingWindowSize", () -> "1");
        registry.add("resilience4j.circuitbreaker.instances.integrationService.permittedNumberOfCallsInHalfOpenState", () -> "1");
        registry.add("resilience4j.circuitbreaker.instances.integrationService.waitDurationInOpenState", () -> "1s");
        registry.add("resilience4j.circuitbreaker.instances.integrationService.automaticTransitionFromOpenToHalfOpenEnabled", () -> "true");
    }

    // Mock beans for required components
    @MockBean
    private IntegrationServiceClient integrationServiceClient;

    @MockBean
    private OAuth2Service oAuth2Service;

    @MockBean
    private OAuth2Controller oAuth2Controller;

    // Changed from @Autowired to @SpyBean
    @SpyBean
    private JwtTokenProvider jwtTokenProvider;

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserService userService;

    @MockBean
    private AuthService authService;

    private User testUser;
    private String accessToken;

    @BeforeEach
    void setUp() throws Exception {
        log.debug("Setting up test data");

        // Clean up users
        userRepository.deleteAll();
        log.debug("Cleared all users from repository");

        // Create test user directly, bypassing UserService
        String encodedPassword = passwordEncoder.encode("Password123");
        log.debug("Created encoded password: {}", encodedPassword);

        testUser = User.create("securitytest", "security@test.com", encodedPassword, "test-ext-123");
        testUser.activate(); // Set to ACTIVE status
        testUser.addRole("ROLE_USER");
        log.debug("Created test user: username={}, email={}, status={}, roles={}",
                testUser.getUsername(), testUser.getEmail(), testUser.getStatus(), testUser.getRoles());

        // Save the user directly using the repository
        testUser = userRepository.save(testUser);
        log.debug("Saved test user with ID: {}", testUser.getId());

        // Verify the user was created properly
        User savedUser = userRepository.findByUsername("securitytest")
                .orElseThrow(() -> new IllegalStateException("Test user not created"));

        log.debug("Verified test user: {}, roles: {}", savedUser.getId(), savedUser.getRoles());

        // Configure mock for integration service client
        CreateUserResponse mockResponse = CreateUserResponse.builder()
                .externalId("test-ext-123")
                .status("ACTIVE")
                .build();

        Mockito.when(integrationServiceClient.createUser(any()))
                .thenReturn(mockResponse);
        log.debug("Configured mock for integration service client");

        // Create token directly using JwtTokenProvider
        Collection<GrantedAuthority> authorities = testUser.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        accessToken = jwtTokenProvider.createAccessToken(
                testUser.getId(),
                testUser.getId().toString(),
                authorities
        );

        log.debug("Created JWT token directly: {} (first 20 chars)",
                accessToken.length() > 20 ? accessToken.substring(0, 20) + "..." : accessToken);

        // Set up mocking of JwtTokenProvider methods - using doReturn for SpyBean
        doReturn(true).when(jwtTokenProvider).validateToken(accessToken);
        doReturn(testUser.getId().toString()).when(jwtTokenProvider).getUsername(accessToken);
        doReturn(false).when(jwtTokenProvider).validateToken("invalid-token");

        // Set up mock responses for user service and auth service
        UserResponse userResponse = UserResponse.builder()
                .id(testUser.getId())
                .username(testUser.getUsername())
                .email(testUser.getEmail())
                .build();

        AuthenticationResponse authResponse = AuthenticationResponse.builder()
                .accessToken("test-token")
                .refreshToken("refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600)
                .user(userResponse)
                .build();

        Mockito.when(userService.getUserById(any())).thenReturn(userResponse);
        Mockito.when(userService.registerUser(any())).thenReturn(userResponse);
        Mockito.when(authService.authenticate(any())).thenReturn(authResponse);
    }

    /**
     * Helper method to create a user authentication token with the proper roles for testing
     */
    private RequestPostProcessor userAuth(String... roles) {
        return SecurityMockMvcRequestPostProcessors.user(testUser.getId().toString()).roles(roles);
    }

    @Test
    void testPublicEndpointAccess() throws Exception {
        log.debug("Running testPublicEndpointAccess");

        // Create proper AuthenticationRequest for test
        AuthenticationRequest authRequest = new AuthenticationRequest();
        authRequest.setUsernameOrEmail("securitytest");
        authRequest.setPassword("Password123");

        // Test login endpoint
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(authRequest)))
                .andExpect(status().isOk());
        log.debug("Login endpoint test passed");

        // Registration endpoint should be accessible without authentication
        mockMvc.perform(post("/api/v1/users/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"newuser\",\"email\":\"new@test.com\",\"password\":\"Password123\"}"))
                .andExpect(status().isCreated());
        log.debug("Registration endpoint test passed");
    }

    @Test
    void testProtectedEndpointRequiresAuthentication() throws Exception {
        log.debug("Running testProtectedEndpointRequiresAuthentication");

        // Protected endpoint should reject unauthenticated requests
        mockMvc.perform(get("/api/v1/users/me"))
                .andExpect(status().isUnauthorized());
        log.debug("Protected endpoint correctly rejected unauthenticated request");
    }

    @Test
    @WithMockUser(username = "securitytest", roles = {"USER"})
    void testProtectedEndpointWithValidToken() throws Exception {
        log.debug("Running testProtectedEndpointWithValidToken");
        log.debug("Using token: {}...", accessToken.substring(0, Math.min(20, accessToken.length())));

        // Protected endpoint should allow authenticated requests with token and proper user context
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + accessToken)
                        .with(userAuth("USER")))
                .andExpect(status().isOk());
        log.debug("Protected endpoint correctly accepted authenticated request");
    }

    @Test
    void testProtectedEndpointWithInvalidToken() throws Exception {
        log.debug("Running testProtectedEndpointWithInvalidToken");

        // Protected endpoint should reject requests with invalid token
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer invalid-token"))
                .andExpect(status().isUnauthorized());
        log.debug("Protected endpoint correctly rejected request with invalid token");
    }

    @Test
    void testTokenExpiration() throws Exception {
        log.debug("Running testTokenExpiration");

        // Create a token with very short expiration
        Map<String, Object> additionalClaims = new HashMap<>();
        Collection<GrantedAuthority> authorities = testUser.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());
        String shortLivedToken = jwtTokenProvider.createToken(
                testUser.getId(), testUser.getId().toString(), authorities, additionalClaims, 1);
        log.debug("Created short-lived token: {}...",
                shortLivedToken.length() > 20 ? shortLivedToken.substring(0, 20) : shortLivedToken);

        // Mock validation of expired token to return false
        doReturn(false).when(jwtTokenProvider).validateToken(shortLivedToken);

        // Sleep for token to expire
        Thread.sleep(10);
        log.debug("Waited for token to expire");

        // Protected endpoint should reject expired token
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + shortLivedToken))
                .andExpect(status().isUnauthorized());
        log.debug("Protected endpoint correctly rejected request with expired token");
    }

    @Test
    void testRoleBasedAuthorization() throws Exception {
        log.debug("Running testRoleBasedAuthorization");

        // Create admin user directly
        String adminPassword = passwordEncoder.encode("AdminPass123");
        User adminUser = User.create("admin", "admin@test.com", adminPassword, "admin-ext-123");
        adminUser.activate();
        adminUser.addRole("ROLE_USER");
        adminUser.addRole("ROLE_ADMIN");
        log.debug("Created admin user: username={}, roles={}", adminUser.getUsername(), adminUser.getRoles());

        // Save directly with repository
        adminUser = userRepository.save(adminUser);
        log.debug("Saved admin user with ID: {}", adminUser.getId());

        // Create admin token directly
        Collection<GrantedAuthority> adminAuthorities = adminUser.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        String adminToken = jwtTokenProvider.createAccessToken(
                adminUser.getId(),
                adminUser.getId().toString(),
                adminAuthorities
        );

        log.debug("Created admin token directly: {} (first 20 chars)",
                adminToken.length() > 20 ? adminToken.substring(0, 20) + "..." : adminToken);

        // Set up validation for admin token
        doReturn(true).when(jwtTokenProvider).validateToken(adminToken);
        doReturn(adminUser.getId().toString()).when(jwtTokenProvider).getUsername(adminToken);

        // Regular user should not access admin endpoint
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + accessToken)
                        .with(userAuth("USER")))
                .andExpect(status().isForbidden());
        log.debug("Regular user correctly denied access to admin endpoint");

        // Admin user should access admin endpoint
        mockMvc.perform(get("/api/v1/admin/users")
                        .header("Authorization", "Bearer " + adminToken)
                        .with(SecurityMockMvcRequestPostProcessors.user(adminUser.getId().toString()).roles("USER", "ADMIN")))
                .andExpect(status().isOk());
        log.debug("Admin user correctly granted access to admin endpoint");
    }
}