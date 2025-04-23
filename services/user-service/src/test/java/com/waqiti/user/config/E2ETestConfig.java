// File: src/test/java/com/waqiti/user/config/E2ETestConfig.java
package com.waqiti.user.config;

import com.waqiti.user.api.OAuth2Controller;
import com.waqiti.user.client.IntegrationServiceClient;
import com.waqiti.user.client.dto.CreateUserResponse;
import com.waqiti.user.service.OAuth2Service;
import com.waqiti.user.security.TestJwtTokenProvider;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProvider;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.DefaultOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.security.oauth2.jwt.JwtDecoder;

import java.time.Instant;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@TestConfiguration
public class E2ETestConfig {

    @Bean
    @Primary
    public TestJwtTokenProvider testJwtTokenProvider() {
        return new TestJwtTokenProvider();
    }

    @Bean
    @Primary
    public OAuth2AuthorizedClientService oAuth2AuthorizedClientService() {
        return Mockito.mock(OAuth2AuthorizedClientService.class);
    }

    @Bean
    @Primary
    public ClientRegistrationRepository clientRegistrationRepository() {
        return Mockito.mock(ClientRegistrationRepository.class);
    }

    @Bean
    @Primary
    public OAuth2AuthorizedClientRepository oAuth2AuthorizedClientRepository() {
        return Mockito.mock(OAuth2AuthorizedClientRepository.class);
    }

    @Bean
    @Primary
    public OAuth2AuthorizedClientManager auth2AuthorizedClientManager(
            ClientRegistrationRepository clientRegistrationRepository,
            OAuth2AuthorizedClientRepository authorizedClientRepository) {

        OAuth2AuthorizedClientProvider authorizedClientProvider =
                OAuth2AuthorizedClientProviderBuilder.builder()
                        .authorizationCode()
                        .refreshToken()
                        .clientCredentials()
                        .password()
                        .build();

        DefaultOAuth2AuthorizedClientManager manager =
                new DefaultOAuth2AuthorizedClientManager(
                        clientRegistrationRepository, authorizedClientRepository);
        manager.setAuthorizedClientProvider(authorizedClientProvider);

        return manager;
    }

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        return Mockito.mock(JwtDecoder.class);
    }

    // IMPORTANT: This is the bean that needs to be fixed
    // Remove the @Primary annotation from the method name and use an explicit bean name
    @Bean(name = "integrationServiceClientMock")
    public IntegrationServiceClient integrationServiceClient() {
        IntegrationServiceClient mockClient = Mockito.mock(IntegrationServiceClient.class);

        // Set up common responses
        CreateUserResponse mockResponse = CreateUserResponse.builder()
                .externalId("ext-test-123")
                .status("ACTIVE")
                .build();

        when(mockClient.createUser(any())).thenReturn(mockResponse);

        return mockClient;
    }

    @Bean
    @Primary
    public OAuth2Service oAuth2Service() {
        return Mockito.mock(OAuth2Service.class);
    }

    @Bean
    @Primary
    public OAuth2Controller oauth2Controller(OAuth2Service oauth2Service) {
        return new OAuth2Controller(oauth2Service);
    }

    // Helper method to create a mock OAuth2AccessToken
    private OAuth2AccessToken createMockAccessToken() {
        return new OAuth2AccessToken(
                OAuth2AccessToken.TokenType.BEARER,
                "mock-token",
                Instant.now(),
                Instant.now().plusSeconds(3600),
                Collections.singleton("read")
        );
    }
}