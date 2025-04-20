/**
 * File: src/test/java/com/waqiti/user/performance/MfaPerformanceTest.java
 * Performance tests for MFA functionality
 */
package com.waqiti.user.performance;

import com.waqiti.user.client.NotificationServiceClient;
import com.waqiti.user.domain.MfaMethod;
import com.waqiti.user.domain.User;
import com.waqiti.user.dto.MfaSetupResponse;
import com.waqiti.user.repository.MfaConfigurationRepository;
import com.waqiti.user.repository.MfaVerificationCodeRepository;
import com.waqiti.user.repository.UserRepository;
import com.waqiti.user.service.MfaService;
import dev.samstevens.totp.code.CodeVerifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@Tag("Performance")
class MfaPerformanceTest {
    @Autowired
    private MfaService mfaService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private MfaConfigurationRepository mfaConfigRepository;

    @Autowired
    private MfaVerificationCodeRepository verificationCodeRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @MockBean
    private NotificationServiceClient notificationServiceClient;

    private List<User> testUsers;
    private static final int NUM_USERS = 100;

    @BeforeEach
    void setup() {
        // Setup for performance testing
        when(notificationServiceClient.sendTwoFactorSms(any())).thenReturn(true);
        when(notificationServiceClient.sendTwoFactorEmail(any())).thenReturn(true);

        // Clean up repositories
        mfaConfigRepository.deleteAll();
        verificationCodeRepository.deleteAll();
        userRepository.deleteAll();

        // Create test users
        testUsers = new ArrayList<>();
        for (int i = 0; i < NUM_USERS; i++) {
            User user = User.create(
                    "perfuser" + i,
                    "perf" + i + "@example.com",
                    passwordEncoder.encode("Password123!"),
                    "ext-perf-" + i);
            user.activate();
            testUsers.add(userRepository.save(user));
        }
    }

    @Test
    @DisplayName("TOTP setup performance should meet threshold")
    void testTotpSetupPerformance() {
        // Measure setup time
        long startTime = System.currentTimeMillis();

        for (User user : testUsers) {
            mfaService.setupTotp(user.getId(), user.getUsername());
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        System.out.println("TOTP setup for " + NUM_USERS + " users took " + totalTime + "ms");
        System.out.println("Average time per user: " + (totalTime / NUM_USERS) + "ms");

        // Acceptable performance threshold - 200ms per user average
        assertTrue(totalTime / NUM_USERS < 200,
                "TOTP setup should take less than 200ms per user on average");
    }

    @Test
    @DisplayName("TOTP verification performance should meet threshold")
    void testTotpVerificationPerformance() {
        // Setup TOTP for all users first
        for (User user : testUsers) {
            MfaSetupResponse setupResponse = mfaService.setupTotp(user.getId(), user.getUsername());

            // We'll use a fixed code for testing since we're mocking verification
            ReflectionTestUtils.setField(mfaService, "codeVerifier", new AlwaysValidCodeVerifier());
        }

        // Measure verification time
        long startTime = System.currentTimeMillis();

        for (User user : testUsers) {
            mfaService.verifyTotpSetup(user.getId(), "123456");
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        System.out.println("TOTP verification for " + NUM_USERS + " users took " + totalTime + "ms");
        System.out.println("Average time per user: " + (totalTime / NUM_USERS) + "ms");

        // Acceptable performance threshold - 100ms per user average
        assertTrue(totalTime / NUM_USERS < 100);
    }

    @Test
    void testSmsSetupPerformance() {
        // Measure setup time
        long startTime = System.currentTimeMillis();

        for (User user : testUsers) {
            mfaService.setupSms(user.getId(), "+1234567890");
        }

        long endTime = System.currentTimeMillis();
        long totalTime = endTime - startTime;

        System.out.println("SMS setup for " + NUM_USERS + " users took " + totalTime + "ms");
        System.out.println("Average time per user: " + (totalTime / NUM_USERS) + "ms");

        // Acceptable performance threshold - 250ms per user average (includes notification service call)
        assertTrue(totalTime / NUM_USERS < 250);
    }

    // Helper class for performance testing
    private static class AlwaysValidCodeVerifier implements CodeVerifier {
        @Override
        public boolean isValidCode(String secret, String code) {
            return true;
        }
    }
}

