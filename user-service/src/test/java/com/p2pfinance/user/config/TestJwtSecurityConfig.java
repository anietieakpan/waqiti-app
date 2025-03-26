// File: src/test/java/com/p2pfinance/user/config/TestJwtSecurityConfig.java
package com.p2pfinance.user.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@TestConfiguration
@EnableWebSecurity
@Order(1) // This ensures our configuration takes precedence
public class TestJwtSecurityConfig {

    // This overrides the tokenSecurityFilterChain in OAuth2SecurityConfig
    @Bean
    @Primary
    public SecurityFilterChain tokenSecurityFilterChain(HttpSecurity http) throws Exception {
        // Do minimal configuration to prevent the original OAuth2SecurityConfig from being used
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth.anyRequest().permitAll())
                .build();
    }

    // Add this JwtDecoder bean to satisfy the dependency
    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        // Create a dummy key for test purposes
        String secret = "dGVzdHNlY3JldGtleWZvcnVuaXR0ZXN0c29ubHlub3Rmb3Jwcm9kdWN0aW9udGVzdHNlY3JldGtleWZvcnVuaXR0ZXN0cw==";
        byte[] keyBytes = Base64.getDecoder().decode(secret);
        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }
}