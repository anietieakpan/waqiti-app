package com.p2pfinance.user.service;

import com.p2pfinance.user.domain.User;
import com.p2pfinance.user.domain.UserNotFoundException;
import com.p2pfinance.user.domain.UserProfile;
import com.p2pfinance.user.dto.AuthenticationResponse;
import com.p2pfinance.user.dto.UserResponse;
import com.p2pfinance.user.repository.UserProfileRepository;
import com.p2pfinance.user.repository.UserRepository;
import com.p2pfinance.user.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class OAuth2Service {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;
    private final JwtTokenProvider tokenProvider;
    private final OAuth2AuthorizedClientService authorizedClientService;

    @Value("${oauth2.state.secret}")
    private String stateSecret;

    @Transactional
    public AuthenticationResponse processOAuthCallback(String code, String state) {
        // Validate state to prevent CSRF
        validateState(state);

        // Process the OAuth code to get user info - simplified for example
        Map<String, Object> attributes = processOAuthCode(code);

        // Find or create user based on OAuth info
        User user = findOrCreateUser(attributes);

        // Generate JWT tokens
        Collection<GrantedAuthority> authorities = user.getRoles().stream()
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

        // Build response
        UserProfile profile = profileRepository.findById(user.getId()).orElse(null);
        UserResponse userResponse = mapToUserResponse(user, profile);

        return AuthenticationResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(tokenProvider.getAccessTokenValidityInSeconds())
                .user(userResponse)
                .build();
    }

    private void validateState(String state) {
        // In a real implementation, verify the state against a stored value
        // to prevent CSRF attacks
        if (state == null || state.isEmpty()) {
            throw new IllegalArgumentException("Invalid OAuth state parameter");
        }
    }

    private Map<String, Object> processOAuthCode(String code) {
        // In a real implementation, this would exchange the code for tokens
        // and retrieve user information
        // Simplified example
        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", UUID.randomUUID().toString());
        attributes.put("email", "oauth_user@example.com");
        attributes.put("name", "OAuth User");
        return attributes;
    }

    private User findOrCreateUser(Map<String, Object> attributes) {
        String email = (String) attributes.get("email");

        // Try to find user by email
        Optional<User> existingUser = userRepository.findByEmail(email);

        if (existingUser.isPresent()) {
            return existingUser.get();
        }

        // Create new user
        String name = (String) attributes.get("name");
        String username = email.substring(0, email.indexOf('@')) + "-" + UUID.randomUUID().toString().substring(0, 8);

        User user = User.create(
                username,
                email,
                UUID.randomUUID().toString(), // Random password, user can't login with it
                "oauth-" + UUID.randomUUID().toString() // External ID placeholder
        );

        user.activate(); // Auto-activate OAuth users
        user = userRepository.save(user);

        // Create profile
        UserProfile profile = UserProfile.create(user);
        String[] nameParts = name.split(" ", 2);
        profile.updateName(
                nameParts.length > 0 ? nameParts[0] : "",
                nameParts.length > 1 ? nameParts[1] : ""
        );
        profileRepository.save(profile);

        return user;
    }

    private UserResponse mapToUserResponse(User user, UserProfile profile) {
        // Implementation similar to the one in UserService
        // Omitted for brevity
        return UserResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .build();
    }
}