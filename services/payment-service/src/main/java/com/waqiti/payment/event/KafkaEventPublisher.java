/**
 * File: ./payment-service/src/main/java/com/waqiti/payment/event/KafkaEventPublisher.java
 */
package com.waqiti.payment.event;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class KafkaEventPublisher implements EventPublisher {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void publishEvent(Object event, String topic, String key) {
        try {
            String jsonEvent = objectMapper.writeValueAsString(event);
            kafkaTemplate.send(topic, key, jsonEvent);
        } catch (Exception e) {
            log.error("Failed to publish event to Kafka", e);
            // Don't rethrow the exception - we want to gracefully handle Kafka failures
        }
    }
}