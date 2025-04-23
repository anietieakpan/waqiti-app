// File: src/test/java/com/waqiti/user/security/TestJwtTokenProvider.java
package com.waqiti.user.security;

import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Base64;

@Component
@Slf4j
public class TestJwtTokenProvider extends JwtTokenProvider {

    // This key is at least 256 bits (32 bytes) as required by HMAC-SHA
    private static final String SECURE_TEST_KEY =
            "ThisIsAVeryLongAndSecureTestKeyThatIsSufficientlyLongForTheHMACSHAAlgorithm123456789";

    // Encode the key in Base64 as expected by the parent class
    private static final String SECURE_TEST_KEY_BASE64 =
            Base64.getEncoder().encodeToString(SECURE_TEST_KEY.getBytes());

    public TestJwtTokenProvider() {
        super();
        log.debug("TestJwtTokenProvider constructor called");
    }

    // Override the init method to use our secure test key
    @Override
    @PostConstruct
    protected void init() {
        // We'll set the secretKey field in the parent class using reflection
        // since it's a private field injected with @Value and doesn't have a setter
        try {
            log.debug("Initializing TestJwtTokenProvider with secure key: {}", SECURE_TEST_KEY.substring(0, 10) + "...");

            java.lang.reflect.Field secretKeyField = JwtTokenProvider.class.getDeclaredField("secretKey");
            secretKeyField.setAccessible(true);
            secretKeyField.set(this, SECURE_TEST_KEY_BASE64);
            secretKeyField.setAccessible(false);

            // Now call the parent's init method which will use our secure key
            super.init();

            log.debug("TestJwtTokenProvider initialization completed successfully");
        } catch (Exception e) {
            log.error("Failed to initialize TestJwtTokenProvider", e);
            throw new RuntimeException("Failed to initialize TestJwtTokenProvider", e);
        }
    }

    // Override validateToken to provide more debugging info
    @Override
    public boolean validateToken(String token) {
        try {
            boolean isValid = super.validateToken(token);
            log.debug("Token validation result: {}", isValid);
            return isValid;
        } catch (Exception e) {
            log.error("Error validating token: {}", e.getMessage());
            return false;
        }
    }
}