package com.waqiti.user.service;

import com.waqiti.user.domain.AuthenticationFailedException;
import com.waqiti.user.domain.User;
import com.waqiti.user.domain.UserNotFoundException;
import com.waqiti.user.dto.AuthenticationRequest;
import com.waqiti.user.dto.AuthenticationResponse;
import com.waqiti.user.dto.TokenRefreshRequest;
import com.waqiti.user.repository.UserRepository;
import com.waqiti.user.repository.UserProfileRepository;
import com.waqiti.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Authenticate a user and generate JWT tokens
     */
    @Transactional
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        return userService.authenticateUser(request);
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