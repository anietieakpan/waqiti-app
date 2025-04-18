/**
 * File: ./payment-service/src/test/java/com/waqiti/payment/config/TestConfig.java
 */
package com.waqiti.payment.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.waqiti.common.event.EventPublisher;
import com.waqiti.payment.event.TestEventPublisher;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;

import static org.mockito.Mockito.mock;

@TestConfiguration
@Profile("test")
public class TestConfig {
    @Bean
    @Primary
    public MeterRegistry meterRegistry() {
        // Use a real SimpleMeterRegistry for tests instead of mocking
        return new SimpleMeterRegistry();
    }

    // Mock KafkaTemplate to avoid real Kafka dependencies
    @Bean
    @Primary
    public KafkaTemplate<String, String> kafkaTemplate() {
        return mock(KafkaTemplate.class);
    }

    // Provide a test event publisher that doesn't actually publish events
    @Bean
    @Primary
    public EventPublisher eventPublisher() {
        return new TestEventPublisher();
    }

    // Configure ObjectMapper with JavaTimeModule for LocalDateTime serialization
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        return objectMapper;
    }
}