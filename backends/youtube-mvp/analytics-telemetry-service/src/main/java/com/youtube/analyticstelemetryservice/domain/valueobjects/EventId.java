package com.youtube.analyticstelemetryservice.domain.valueobjects;

import lombok.Value;

import java.util.UUID;

/**
 * Value object representing a unique event identifier.
 * Immutable and validated on construction.
 */
@Value
public class EventId {
    String value;
    
    public EventId(String value) {
        if (value == null || value.isBlank()) {
            this.value = UUID.randomUUID().toString();
        } else {
            this.value = value;
        }
    }
    
    public static EventId generate() {
        return new EventId(UUID.randomUUID().toString());
    }
    
    public static EventId of(String value) {
        return new EventId(value);
    }
}

