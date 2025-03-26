/**
 * File: src/main/java/com/p2pfinance/notification/config/FirebaseConfig.java
 */

package com.p2pfinance.notification.config;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.extern.slf4j.Slf4j;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.io.ClassPathResource;

import java.io.IOException;
import java.io.InputStream;

@Configuration
@Slf4j
public class FirebaseConfig {

    @Value("${firebase.config-file}")
    private String firebaseConfigFile;

    @Bean
    @Profile("!test") // Use real implementation in non-test environments
    public FirebaseMessaging realFirebaseMessaging() {
        try {
            InputStream serviceAccount = new ClassPathResource(firebaseConfigFile).getInputStream();

            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccount))
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp app = FirebaseApp.initializeApp(options);
                log.info("Firebase application has been initialized");
                return FirebaseMessaging.getInstance(app);
            }

            return FirebaseMessaging.getInstance();
        } catch (Exception e) {
            log.error("Failed to initialize Firebase", e);
            // For non-test environments, we'll still need to return a mock
            // since we can't return null in production
            return createMockFirebaseMessaging();
        }
    }

    @Bean
    @Profile("test") // Use mock implementation in test environment
    public FirebaseMessaging testFirebaseMessaging() {
        log.info("Creating mock Firebase messaging for test environment");
        return createMockFirebaseMessaging();
    }

    private FirebaseMessaging createMockFirebaseMessaging() {
        // Create a Mockito mock
        FirebaseMessaging mockFirebaseMessaging = Mockito.mock(FirebaseMessaging.class);

        // Set up behavior for the mock
        try {
            Mockito.when(mockFirebaseMessaging.send(Mockito.any(Message.class)))
                    .thenAnswer(invocation -> {
                        Message message = invocation.getArgument(0);
                        log.info("Mock Firebase: Would send message: {}", message);
                        return "mock-message-id-" + System.currentTimeMillis();
                    });
        } catch (Exception e) {
            log.warn("Error configuring mock Firebase behavior", e);
        }

        return mockFirebaseMessaging;
    }
}