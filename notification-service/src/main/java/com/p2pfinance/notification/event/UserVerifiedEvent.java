/**
 * File: src/main/java/com/p2pfinance/notification/event/UserVerifiedEvent.java
 */
package com.p2pfinance.notification.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.EqualsAndHashCode;

import javax.annotation.PostConstruct;
import java.util.UUID;

/**
 * Event for user verification
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = false)
public class UserVerifiedEvent extends NotificationEvent {
    private UUID userId;
    private String username;

    // You can add a method to set the event type after construction
    @PostConstruct
    private void init() {
        this.setEventType("USER_VERIFIED");
    }

    // For manually setting the event type when not using Spring
    public void initializeEventType() {
        this.setEventType("USER_VERIFIED");
    }
}