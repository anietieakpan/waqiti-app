// File: src/test/java/com/waqiti/user/config/TestConfig.java
package com.waqiti.user.config;

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