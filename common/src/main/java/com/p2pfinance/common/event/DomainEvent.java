// File: common/src/main/java/com/p2pfinance/common/event/DomainEvent.java
package com.p2pfinance.common.event;

import java.time.LocalDateTime;

public interface DomainEvent {
    String getEventId();
    String getEventType();
    LocalDateTime getTimestamp();
    String getTopic();
}