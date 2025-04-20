/**
 * File: src/test/java/com/waqiti/user/service/AuthServiceMfaTest.java
 * Unit tests for MFA functionality in Auth Service
 */
package com.waqiti.user.service;

import com.waqiti.user.domain.AuthenticationFailedException;
import com.waqiti.user.domain.MfaMethod;
import com.waqiti.user.domain.User;
import com.waqiti.user.domain.UserStatus;
import com.waqiti.user.dto.AuthenticationRequest;
import com.waqiti.user.dto.AuthenticationResponse;
import com.waqiti.user.dto.MfaVerifyRequest;
import com.waqiti.user.dto.UserResponse;
import com.waqiti.user.repository.UserProfileRepository;
import com.waqiti.user.repository.UserRepository;
import com.waqiti.user.security.JwtTokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceMfaTest {
    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository profileRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private MfaService mfaService;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setup() {
        // Setup common test data
    }

    @Test
    @DisplayName("Authentication should require MFA when enabled")
    void testAuthenticate_WithMfaEnabled() {
        // Given
        UUID userId = UUID.randomUUID();
        String username = "testuser";
        String password = "password";

        AuthenticationRequest request = new AuthenticationRequest(username, password);

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        username, password, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        when(authentication.getPrincipal()).thenReturn(userDetails);

        User user = User.create(username, "user@example.com", "$2a$10$hashed", "ext-1");
        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(user, "status", UserStatus.ACTIVE);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // MFA is enabled for this user
        when(mfaService.isMfaEnabled(userId)).thenReturn(true);
        List<MfaMethod> enabledMethods = Arrays.asList(MfaMethod.TOTP, MfaMethod.SMS);
        when(mfaService.getEnabledMfaMethods(userId)).thenReturn(enabledMethods);

        // Mock JWT provider for MFA token
        Map<String, Object> mfaClaims = new HashMap<>();
        mfaClaims.put("mfa_required", true);
        String mfaToken = "mfa-jwt-token";
        when(tokenProvider.createToken(
                eq(userId), eq(username), any(), argThat(map -> map.containsKey("mfa_required")), anyLong()))
                .thenReturn(mfaToken);

        // When
        AuthenticationResponse response = authService.authenticate(request);

        // Then
        assertNotNull(response);
        assertTrue(response.isRequiresMfa());
        assertEquals(mfaToken, response.getMfaToken());
        assertEquals(enabledMethods, response.getAvailableMfaMethods());
        assertNull(response.getAccessToken());
        assertNull(response.getRefreshToken());
    }

    @Test
    @DisplayName("Authentication should not require MFA when disabled")
    void testAuthenticate_WithoutMfa() {
        // Given
        UUID userId = UUID.randomUUID();
        String username = "testuser";
        String password = "password";

        AuthenticationRequest request = new AuthenticationRequest(username, password);

        Authentication authentication = mock(Authentication.class);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);

        org.springframework.security.core.userdetails.User userDetails =
                new org.springframework.security.core.userdetails.User(
                        username, password, Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER")));
        when(authentication.getPrincipal()).thenReturn(userDetails);

        User user = User.create(username, "user@example.com", "$2a$10$hashed", "ext-1");
        ReflectionTestUtils.setField(user, "id", userId);
        ReflectionTestUtils.setField(user, "status", UserStatus.ACTIVE);
        when(userRepository.findByUsername(username)).thenReturn(Optional.of(user));

        // MFA is NOT enabled for this user
        when(mfaService.isMfaEnabled(userId)).thenReturn(false);

        // Mock JWT provider for regular tokens
        String accessToken = "access-token";
        String refreshToken = "refresh-token";
        when(tokenProvider.createAccessToken(eq(userId), eq(username), any()))
                .thenReturn(accessToken);
        when(tokenProvider.createRefreshToken(userId, username))
                .thenReturn(refreshToken);
        when(tokenProvider.getAccessTokenValidityInSeconds()).thenReturn(3600L);

        // When
        AuthenticationResponse response = authService.authenticate(request);

        // Then
        assertNotNull(response);
        assertFalse(response.isRequiresMfa());
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
    }

    @Test
    @DisplayName("MFA verification should succeed with valid code")
    void testVerifyMfa_Success() {
        // Given
        UUID userId = UUID.randomUUID();
        String username = "testuser";
        MfaMethod method = MfaMethod.TOTP;
        String code = "123456";
        String mfaToken = "mfa-jwt-token";

        MfaVerifyRequest request = new MfaVerifyRequest(method, code);

        // Mock token provider
        when(tokenProvider.validateToken(mfaToken)).thenReturn(true);
        when(tokenProvider.getUserId(mfaToken)).thenReturn(userId);
        when(tokenProvider.getUsername(mfaToken)).thenReturn(username);


                // Change to:
        Claims claims = Jwts.claims();
        claims.put("mfa_required", true);
        when(tokenProvider.getClaimsFromToken(mfaToken)).thenReturn(claims);


        // Mock MFA verification
        when(mfaService.verifyMfaCode(userId, method, code)).thenReturn(true);

        // Mock user
        User user = User.create(username, "user@example.com", "$2a$10$hashed", "ext-1");
        ReflectionTestUtils.setField(user, "id", userId);
        user.addRole("ROLE_USER");
        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        // Mock userService.getUserById
        UserResponse userResponse = UserResponse.builder()
                .id(userId)
                .username(username)
                .build();
        when(userService.getUserById(userId)).thenReturn(userResponse);

        // Mock tokens for successful authentication
        String accessToken = "access-token";
        String refreshToken = "refresh-token";
        when(tokenProvider.createAccessToken(eq(userId), eq(username), any()))
                .thenReturn(accessToken);
        when(tokenProvider.createRefreshToken(userId, username))
                .thenReturn(refreshToken);
        when(tokenProvider.getAccessTokenValidityInSeconds()).thenReturn(3600L);

        // When
        AuthenticationResponse response = authService.verifyMfa(mfaToken, request);

        // Then
        assertNotNull(response);
        assertFalse(response.isRequiresMfa());
        assertEquals(accessToken, response.getAccessToken());
        assertEquals(refreshToken, response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals(3600L, response.getExpiresIn());
    }

    @Test
    @DisplayName("MFA verification should fail with invalid token")
    void testVerifyMfa_InvalidMfaToken() {
        // Given
        MfaMethod method = MfaMethod.TOTP;
        String code = "123456";
        String mfaToken = "invalid-token";

        MfaVerifyRequest request = new MfaVerifyRequest(method, code);

        // Mock token provider
        when(tokenProvider.validateToken(mfaToken)).thenReturn(false);

        // When/Then
        assertThrows(AuthenticationFailedException.class, () ->
                authService.verifyMfa(mfaToken, request));
    }
}