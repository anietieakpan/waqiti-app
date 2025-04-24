// File: src/test/java/com/waqiti/user/config/MfaTestConfig.java
package com.waqiti.user.config;

import com.waqiti.user.client.NotificationServiceClient;
import com.waqiti.user.domain.MfaConfiguration;
import com.waqiti.user.domain.MfaMethod;
import com.waqiti.user.domain.MfaVerificationCode;
import com.waqiti.user.dto.MfaSetupResponse;
import com.waqiti.user.repository.MfaConfigurationRepository;
import com.waqiti.user.repository.MfaVerificationCodeRepository;
import com.waqiti.user.security.TestJwtTokenProvider;
import com.waqiti.user.service.MfaService;
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
import java.util.UUID;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

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

    @Bean
    @Primary
    public MfaService mockMfaService(
            MfaConfigurationRepository mfaConfigRepository,
            MfaVerificationCodeRepository verificationCodeRepository,
            NotificationServiceClient notificationServiceClient) {

        // Create a mock of the MfaService
        MfaService mockService = Mockito.mock(MfaService.class);

        // Configure the mock to return appropriate values
        when(mockService.isMfaEnabled(any(UUID.class))).thenReturn(false);

        when(mockService.getEnabledMfaMethods(any(UUID.class)))
                .thenReturn(Arrays.asList(MfaMethod.TOTP, MfaMethod.SMS));

        when(mockService.verifyMfaCode(any(UUID.class), any(MfaMethod.class), anyString()))
                .thenReturn(true);

        when(mockService.setupTotp(any(UUID.class), anyString()))
                .thenReturn(MfaSetupResponse.builder()
                        .secret("ABCDEFGHIJKLMNOPQRSTUVWXYZ")
                        .qrCodeImage("mock-qr-code-data")
                        .build());

        when(mockService.verifyTotpSetup(any(UUID.class), anyString()))
                .thenReturn(true);

        when(mockService.setupSms(any(UUID.class), anyString()))
                .thenReturn(true);

        when(mockService.setupEmail(any(UUID.class), anyString()))
                .thenReturn(true);

        when(mockService.verifyCodeSetup(any(UUID.class), any(MfaMethod.class), anyString()))
                .thenReturn(true);

        when(mockService.resendVerificationCode(any(UUID.class), any(MfaMethod.class)))
                .thenReturn(true);

        when(mockService.disableMfaMethod(any(UUID.class), any(MfaMethod.class)))
                .thenReturn(true);

        when(mockService.generateRecoveryCodes(any(UUID.class), anyInt()))
                .thenReturn(new UUID[]{UUID.randomUUID(), UUID.randomUUID()});

        when(mockService.verifyRecoveryCode(any(UUID.class), anyString()))
                .thenReturn(true);

        when(mockService.resetUserMfa(any(UUID.class)))
                .thenReturn(true);

        return mockService;
    }

    @Bean
    public MfaVerificationCodeRepository.FindLatestActiveCodeMethod findLatestActiveCodeMethod() {
        return (userId, method, now) -> {
            // Create a mock verification code
            MfaVerificationCode code = MfaVerificationCode.create(
                    userId, method, "123456", 30);
            return Optional.of(code);
        };
    }
}