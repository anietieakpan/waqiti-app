// File: src/test/java/com/waqiti/user/config/TestSecurityConfig.java
package com.waqiti.user.config;

import com.waqiti.user.security.JwtAuthenticationFilter;
import com.waqiti.user.security.TestJwtTokenProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
public class TestSecurityConfig {

    @Autowired
    private TestJwtTokenProvider testJwtTokenProvider;

    @Bean
    @Primary
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            UserDetailsService userDetailsService) throws Exception {

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/users/register").permitAll()
                        .requestMatchers("/api/v1/users/verify/**").permitAll()
                        .requestMatchers("/api/v1/users/password/reset/**").permitAll()
                        .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                // Explicitly disable OAuth2 configuration
                .oauth2ResourceServer(AbstractHttpConfigurer::disable)
                .oauth2Client(AbstractHttpConfigurer::disable)
                // Add our custom JWT filter with TestJwtTokenProvider
                .addFilterBefore(
                        new JwtAuthenticationFilter(testJwtTokenProvider, userDetailsService),
                        UsernamePasswordAuthenticationFilter.class
                )
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    // Use a secure key for the JwtDecoder
    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        // Create a secure key for test purposes (at least 256 bits)
        String strongKey = "ThisIsAVeryLongAndSecureTestKeyThatIsSufficientlyLongForTheHMACSHAAlgorithm123456789";
        byte[] keyBytes = Base64.getEncoder().encodeToString(strongKey.getBytes()).getBytes();
        SecretKey secretKey = new SecretKeySpec(keyBytes, "HmacSHA256");
        return NimbusJwtDecoder.withSecretKey(secretKey).build();
    }
}