/**
 * File: ./payment-service/src/test/java/com/p2pfinance/payment/config/TestSecurityConfig.java
 */
package com.p2pfinance.payment.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@TestConfiguration
@EnableWebSecurity
@Profile("test")
public class TestSecurityConfig {

    // Constant UUID for test user that can be referenced in tests
    public static final String TEST_USER_UUID = "3da1bd8c-d04b-4eee-b8ab-c7a8c1142599";

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Disable CSRF for tests
        http.csrf(csrf -> csrf.disable());
        // Configure other security settings as needed
        http.authorizeHttpRequests(auth -> auth.anyRequest().permitAll());
        return http.build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        // Create user with UUID as username
        UserDetails testUser = User.builder()
                .username(TEST_USER_UUID)
                .password("{noop}password")
                .roles("USER")
                .build();

        return new InMemoryUserDetailsManager(testUser);
    }
}