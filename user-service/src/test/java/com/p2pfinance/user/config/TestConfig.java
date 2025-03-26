// File: src/test/java/com/p2pfinance/user/config/TestConfig.java
package com.p2pfinance.user.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;

/**
 * Test-specific configuration for user service.
 */
@TestConfiguration
@Profile("test")
public class TestConfig {
    // Add any test-specific beans here if needed
}