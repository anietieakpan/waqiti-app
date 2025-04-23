// File: src/test/java/com/waqiti/user/config/OAuth2TestConfig.java
package com.waqiti.user.config;

import com.waqiti.user.api.OAuth2Controller;
import com.waqiti.user.service.OAuth2Service;
import org.mockito.Mockito;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClientService;

@TestConfiguration
public class OAuth2TestConfig {

    @Bean
    @Primary
    public OAuth2Controller oAuth2Controller() {
        return Mockito.mock(OAuth2Controller.class);
    }

    @Bean
    @Primary
    public OAuth2Service oAuth2Service() {
        return Mockito.mock(OAuth2Service.class);
    }

    @Bean
    @Primary
    public OAuth2AuthorizedClientService oAuth2AuthorizedClientService() {
        return Mockito.mock(OAuth2AuthorizedClientService.class);
    }
}