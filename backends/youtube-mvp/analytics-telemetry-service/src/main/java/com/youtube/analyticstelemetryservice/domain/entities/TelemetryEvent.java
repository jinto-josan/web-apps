package com.youtube.analyticstelemetryservice.domain.entities;

import com.youtube.analyticstelemetryservice.domain.valueobjects.EventId;
import com.youtube.analyticstelemetryservice.domain.valueobjects.EventSchema;
import com.youtube.analyticstelemetryservice.domain.valueobjects.EventSource;
import com.youtube.analyticstelemetryservice.domain.valueobjects.EventType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * Domain entity representing a telemetry event.
 * Immutable domain object that encapsulates event data.
 */
@Data
@Builder
public class TelemetryEvent {
    
    private EventId eventId;
    private EventType eventType;
    private EventSource eventSource;
    private EventSchema schema;
    private Instant timestamp;
    private String userId;
    private String sessionId;
    private Map<String, Object> properties;
    private String correlationId;
    
    /**
     * Validates the event according to domain rules.
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        if (eventId == null) {
            throw new IllegalArgumentException("Event ID is required");
        }
        if (eventType == null) {
            throw new IllegalArgumentException("Event type is required");
        }
        if (eventSource == null) {
            throw new IllegalArgumentException("Event source is required");
        }
        if (timestamp == null) {
            throw new IllegalArgumentException("Timestamp is required");
        }
        if (schema == null) {
            throw new IllegalArgumentException("Schema is required");
        }
    }
}

