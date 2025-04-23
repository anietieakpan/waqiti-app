// File: src/test/java/com/waqiti/user/config/TestSecurityConfiguration.java
package com.waqiti.user.config;

import com.waqiti.user.security.JwtAuthenticationFilter;
import com.waqiti.user.security.TestJwtTokenProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@Slf4j
public class TestSecurityConfiguration {

    @Bean
    @Primary
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            TestJwtTokenProvider tokenProvider,
            UserDetailsService userDetailsService) throws Exception {

        log.debug("Configuring test security filter chain with all paths permitted");

        // For testing, permit all requests and disable CSRF protection
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/**").permitAll()  // Allow all requests during testing
                )
                // Still add our JWT filter for token processing
                .addFilterBefore(
                        new JwtAuthenticationFilter(tokenProvider, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }

    @Bean
    @Primary
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        log.debug("Creating test authentication manager");
        return authConfig.getAuthenticationManager();
    }
}