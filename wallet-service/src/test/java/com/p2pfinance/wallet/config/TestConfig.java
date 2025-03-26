/**
 * File: ./wallet-service/src/test/java/com/p2pfinance/wallet/config/TestConfig.java
 */
package com.p2pfinance.wallet.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.web.client.RestTemplate;

/**
 * Test-specific configuration for wallet service.
 * Provides beans needed for integration testing, including
 * mock services and API configurations.
 */
@TestConfiguration
public class TestConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }

    // Mock exchange rate API configuration for testing
    @Bean
    public String exchangeRateApiUrl() {
        return "http://mock-exchange-rate-api.com";
    }

    @Bean
    public String apiKey() {
        return "test-api-key";
    }
}