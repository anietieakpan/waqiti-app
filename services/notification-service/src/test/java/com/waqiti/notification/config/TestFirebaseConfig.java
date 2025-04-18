/**
 * File: src/test/java/com/waqiti/notification/config/TestFirebaseConfig.java
 */
package com.waqiti.notification.config;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.Message;
import lombok.extern.slf4j.Slf4j;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;

@Configuration
@Profile("test")
@Slf4j
public class TestFirebaseConfig {

    @Bean
    @Primary
    public FirebaseMessaging testFirebaseMessaging() {
        log.info("Creating mock Firebase messaging for test environment");

        // Create a Mockito mock
        FirebaseMessaging mockFirebaseMessaging = Mockito.mock(FirebaseMessaging.class);

        // Set up default behavior if needed
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