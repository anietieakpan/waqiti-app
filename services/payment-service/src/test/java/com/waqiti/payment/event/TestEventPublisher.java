/**
 * File: ./payment-service/src/test/java/com/waqiti/payment/event/TestEventPublisher.java
 */
package com.waqiti.payment.event;

import com.waqiti.common.event.DomainEvent;
import com.waqiti.common.event.EventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Profile;

import java.util.Map;

/**
 * Test implementation of EventPublisher that logs events instead of sending them to Kafka
 * Note: This class is instantiated by TestConfig, not through component scanning
 */
@Slf4j
@Profile("test")
public class TestEventPublisher extends EventPublisher {

    public TestEventPublisher() {
        // Call superclass constructor with null values since they won't be used
        super(null, null);
    }

    /**
     * Override the publishEvent method to just log the event instead of publishing to Kafka
     */
    @Override
    public void publishEvent(String topic, String key, String eventType, Map<String, Object> data) {
        // Using our own logger instead of the parent class logger which is private
        log.info("TEST EVENT PUBLISHER: Would publish event to topic '{}' with key '{}', type '{}', data: {}",
                topic, key, eventType, data);
        // No-op implementation for tests
    }

    /**
     * Override the publishDomainEvent method to just log the event instead of publishing to Kafka
     */
    @Override
    public void publishDomainEvent(String topic, DomainEvent event) {
        // Using our own logger instead of the parent class logger which is private
        log.info("TEST EVENT PUBLISHER: Would publish domain event to topic '{}', type '{}', eventId: {}",
                topic, event.getEventType(), event.getEventId());
        // No-op implementation for tests
    }
}