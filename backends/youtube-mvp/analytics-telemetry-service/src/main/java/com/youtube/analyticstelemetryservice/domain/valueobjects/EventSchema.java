package com.youtube.analyticstelemetryservice.domain.valueobjects;

import lombok.Value;

/**
 * Value object representing the schema version of an event.
 * Used for schema validation and evolution.
 */
@Value
public class EventSchema {
    String version;
    String name;
    
    public EventSchema(String version, String name) {
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("Schema version cannot be null or blank");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Schema name cannot be null or blank");
        }
        this.version = version;
        this.name = name;
    }
    
    public static EventSchema of(String version, String name) {
        return new EventSchema(version, name);
    }
    
    public static final EventSchema V1 = new EventSchema("1.0", "telemetry-event-v1");
    
    @Override
    public String toString() {
        return name + "-" + version;
    }
}

