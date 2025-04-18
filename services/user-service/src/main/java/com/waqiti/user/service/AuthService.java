package com.waqiti.user.service;

import com.waqiti.user.domain.AuthenticationFailedException;
import com.waqiti.user.domain.MfaMethod;
import com.waqiti.user.domain.User;
import com.waqiti.user.domain.UserNotFoundException;
import com.waqiti.user.dto.AuthenticationRequest;
import com.waqiti.user.dto.AuthenticationResponse;
import com.waqiti.user.dto.MfaVerifyRequest;
import com.waqiti.user.dto.TokenRefreshRequest;
import com.waqiti.user.repository.UserRepository;
import com.waqiti.user.repository.UserProfileRepository;
import com.waqiti.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider tokenProvider;
    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final UserService userService;
    private final MfaService mfaService;

    /**
     * Authenticate a user and generate JWT tokens
     */
    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        log.info("Authenticating user: {}", request.getUsernameOrEmail());

        // Authenticate with Spring Security
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsernameOrEmail(),
                        request.getPassword()
                )
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);

        // Get user details from the authenticated principal
        org.springframework.security.core.userdetails.User userDetails =
                (org.springframework.security.core.userdetails.User) authentication.getPrincipal();

        // Get our user entity
        User user = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new UserNotFoundException("User not found after authentication: " + userDetails.getUsername()));

        // If the user is not active, reject authentication
        if (!user.isActive()) {
            throw new AuthenticationFailedException("User account is not active");
        }

        // Check if MFA is enabled for this user
        boolean mfaEnabled = mfaService.isMfaEnabled(user.getId());

        if (mfaEnabled) {
            // If MFA is enabled, return a partial response with MFA token
            List<MfaMethod> enabledMethods = mfaService.getEnabledMfaMethods(user.getId());

            // Generate a short-lived MFA token
            Map<String, Object> claims = new HashMap<>();
            claims.put("mfa_required", true);

            String mfaToken = tokenProvider.createToken(
                    user.getId(),
                    user.getUsername(),
                    userDetails.getAuthorities(),
                    claims,
                    5 * 60 * 1000 // 5 minutes expiry for MFA token
            );

            return AuthenticationResponse.builder()
                    .mfaToken(mfaToken)
                    .requiresMfa(true)
                    .availableMfaMethods(enabledMethods)
                    .user(userService.getUserById(user.getId()))
                    .build();
        } else {
            // If MFA is not enabled, return a full token response
            String accessToken = tokenProvider.createAccessToken(
                    user.getId(),
                    user.getUsername(),
                    userDetails.getAuthorities()
            );

            String refreshToken = tokenProvider.createRefreshToken(
                    user.getId(),
                    user.getUsername()
            );

            return AuthenticationResponse.builder()
                    .accessToken(accessToken)
                    .refreshToken(refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(tokenProvider.getAccessTokenValidityInSeconds())
                    .requiresMfa(false)
                    .user(userService.getUserById(user.getId()))
                    .build();

//            return userService.authenticateUser(request);
        }
    }


    /**
     * Complete authentication with MFA verification
     */
    @Transactional
    public AuthenticationResponse verifyMfa(String mfaToken, MfaVerifyRequest request) {
        log.info("Verifying MFA for token with method: {}", request.getMethod());

        // Validate MFA token
        if (!tokenProvider.validateToken(mfaToken)) {
            throw new AuthenticationFailedException("Invalid or expired MFA token");
        }

        // Extract user info from token
        UUID userId = tokenProvider.getUserId(mfaToken);
        String username = tokenProvider.getUsername(mfaToken);

        // Verify the MFA claim is present in the token
        Map<String, Object> claims = tokenProvider.getClaimsFromToken(mfaToken);
        if (claims == null || !Boolean.TRUE.equals(claims.get("mfa_required"))) {
            throw new AuthenticationFailedException("Invalid MFA token");
        }

        // Verify the MFA code
        boolean verified = mfaService.verifyMfaCode(userId, request.getMethod(), request.getCode());

        if (!verified) {
            throw new AuthenticationFailedException("Invalid MFA code");
        }

        // Get the user
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));

        // Generate the real tokens after successful MFA verification
        List<SimpleGrantedAuthority> authorities = user.getRoles().stream()
                .map(SimpleGrantedAuthority::new)
                .collect(Collectors.toList());

        String accessToken = tokenProvider.createAccessToken(
                user.getId(),
                user.getUsername(),
                authorities
        );

        String refreshToken = tokenProvider.createRefreshToken(
                user.getId(),
                user.getUsername()
        );

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenValidityInSeconds())
                .requiresMfa(false)
                .user(userService.getUserById(user.getId()))
                .build();
    }



    /**
     * Refresh an access token using a refresh token
     */
    @Transactional
    public AuthenticationResponse refreshToken(TokenRefreshRequest request) {
        log.info("Refreshing token");
        
        // Validate refresh token
        if (!tokenProvider.validateToken(request.getRefreshToken())) {
            throw new AuthenticationFailedException("Invalid refresh token");
        }
        
        // Extract user ID and username from refresh token
        UUID userId = tokenProvider.getUserId(request.getRefreshToken());
        String username = tokenProvider.getUsername(request.getRefreshToken());
        
        // Get user details
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException(userId));
        
        // Verify username matches
        if (!user.getUsername().equals(username)) {
            throw new AuthenticationFailedException("Token does not match user");
        }
        
        // Verify user is active
        if (!user.isActive()) {
            throw new AuthenticationFailedException("User account is not active");
        }
        
        // Generate new tokens
        String accessToken = tokenProvider.createAccessToken(
                user.getId(),
                user.getUsername(),
                user.getRoles().stream()
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList())
        );
        
        String refreshToken = tokenProvider.createRefreshToken(
                user.getId(),
                user.getUsername()
        );
        
        // Build response
        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenValidityInSeconds())
                .user(userService.getUserById(user.getId()))
                .build();
    }

    /**
     * Logout a user
     */
    public void logout(String refreshToken) {
        // In a real implementation, we might want to blacklist the refresh token
        // For now, just clear the security context
        SecurityContextHolder.clearContext();
    }
}