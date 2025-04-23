// File: src/test/java/com/waqiti/user/config/MfaTestConfig.java
package com.waqiti.user.config;

import com.waqiti.user.security.TestJwtTokenProvider;
import org.mockito.Mockito;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.context.annotation.Primary;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@TestConfiguration
@EnableJpaRepositories(basePackages = "com.waqiti.user.repository")
@EntityScan(basePackages = "com.waqiti.user.domain")
@ComponentScan(basePackages = {"com.waqiti.user.repository"})
@Import(TestSecurityConfiguration.class)
public class MfaTestConfig {

    private static final String TEST_KEY = "VGhpc0lzQVZlcnlMb25nQW5kU2VjdXJlVGVzdEtleVRoYXRJc1N1ZmZpY2llbnRseUxvbmdGb3JUaGVITUFDU0hBQWxnb3JpdGhtMTIzNDU2Nzg5";

    @Bean
    @Primary
    public TestJwtTokenProvider testJwtTokenProvider() {
        return new TestJwtTokenProvider();
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        // Create a dummy key for test purposes
        byte[] keyBytes = Base64.getDecoder().decode(TEST_KEY);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }
}