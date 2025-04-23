// File: src/test/java/com/waqiti/user/config/TestOAuth2Config.java
package com.waqiti.user.config;

import com.waqiti.user.dto.AuthenticationResponse;
import com.waqiti.user.dto.UserResponse;
import com.waqiti.user.repository.UserProfileRepository;
import com.waqiti.user.repository.UserRepository;
import com.waqiti.user.security.TestJwtTokenProvider;
import com.waqiti.user.service.OAuth2Service;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@TestConfiguration
public class TestOAuth2Config {

    @Bean
    @Primary
    public OAuth2Service oAuth2Service() {
        // Create a mock instead of trying to spy the real implementation
        OAuth2Service mockService = Mockito.mock(OAuth2Service.class);

        // Mock the methods that would be called by the tests
        AuthenticationResponse mockResponse = AuthenticationResponse.builder()
                .accessToken("test-access-token")
                .refreshToken("test-refresh-token")
                .tokenType("Bearer")
                .expiresIn(3600)
                .user(UserResponse.builder()
                        .id(UUID.randomUUID())
                        .username("test-user")
                        .email("test@example.com")
                        .build())
                .build();

        when(mockService.processOAuthCallback(anyString(), anyString())).thenReturn(mockResponse);

        return mockService;
    }
}