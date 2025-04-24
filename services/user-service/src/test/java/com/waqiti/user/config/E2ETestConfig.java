// File: src/test/java/com/waqiti/user/config/E2ETestConfig.java
package com.waqiti.user.config;

import com.waqiti.user.client.IntegrationServiceClient;
import com.waqiti.user.client.NotificationServiceClient;
import com.waqiti.user.client.dto.CreateUserResponse;
import com.waqiti.user.service.OAuth2Service;
import com.waqiti.user.security.TestJwtTokenProvider;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;

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
    public NotificationServiceClient notificationServiceClient() {
        NotificationServiceClient mockClient = Mockito.mock(NotificationServiceClient.class);
        when(mockClient.sendTwoFactorSms(any())).thenReturn(true);
        when(mockClient.sendTwoFactorEmail(any())).thenReturn(true);
        return mockClient;
    }

    @Bean
    @Primary
    public OAuth2Service oAuth2Service() {
        return Mockito.mock(OAuth2Service.class);
    }
}