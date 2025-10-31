package com.youtube.analyticstelemetryservice.domain.valueobjects;

import lombok.Value;

/**
 * Value object representing the source of an event (client/server).
 */
@Value
public class EventSource {
    String value;
    boolean isClient;
    
    public EventSource(String value, boolean isClient) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Event source cannot be null or blank");
        }
        this.value = value;
        this.isClient = isClient;
    }
    
    public static EventSource client(String value) {
        return new EventSource(value, true);
    }
    
    public static EventSource server(String value) {
        return new EventSource(value, false);
    }
    
    public static final EventSource WEB_CLIENT = client("web");
    public static final EventSource MOBILE_CLIENT = client("mobile");
    public static final EventSource SERVER = server("server");
    public static final EventSource API = server("api");
}

