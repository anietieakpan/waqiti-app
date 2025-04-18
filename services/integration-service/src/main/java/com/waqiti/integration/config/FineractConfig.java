package com.waqiti.integration.config;

import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.ApiClient;
import org.apache.fineract.client.auth.ApiKeyAuth;
import org.apache.fineract.client.auth.HttpBasicAuth;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "fineract")
@Data
@Slf4j
public class FineractConfig {
    private String baseUrl;
    private String username;
    private String password;
    private String tenantId;
    private int connectionTimeout = 60000;
    private int readTimeout = 60000;

    @Bean
    public ApiClient fineractApiClient() {
        log.info("Initializing Fineract API client with base URL: {}", baseUrl);

        ApiClient apiClient = new ApiClient();
        apiClient.setBasePath(baseUrl);
        apiClient.setConnectTimeout(connectionTimeout);
        apiClient.setReadTimeout(readTimeout);

        // Set up authentication
        HttpBasicAuth basicAuth = (HttpBasicAuth) apiClient.getAuthentication("basicAuth");
        basicAuth.setUsername(username);
        basicAuth.setPassword(password);

        // Set tenant ID
        ApiKeyAuth tenantAuth = (ApiKeyAuth) apiClient.getAuthentication("tenantid");
        tenantAuth.setApiKey(tenantId);

        return apiClient;
    }
}