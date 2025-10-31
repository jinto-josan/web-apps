package com.youtube.analyticstelemetryservice.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * DTO for incoming telemetry event requests.
 */
@Data
public class TelemetryEventRequest {
    
    @JsonProperty("event_id")
    private String eventId; // Optional - will be generated if not provided
    
    @NotBlank(message = "Event type is required")
    @JsonProperty("event_type")
    private String eventType;
    
    @NotBlank(message = "Event source is required")
    @JsonProperty("event_source")
    private String eventSource;
    
    @JsonProperty("is_client")
    private Boolean isClient; // Optional - defaults based on source
    
    @NotNull(message = "Timestamp is required")
    @JsonProperty("timestamp")
    private Instant timestamp;
    
    @JsonProperty("schema_version")
    private String schemaVersion; // Optional - defaults to "1.0"
    
    @JsonProperty("schema_name")
    private String schemaName; // Optional - defaults to "telemetry-event-v1"
    
    @JsonProperty("user_id")
    private String userId;
    
    @JsonProperty("session_id")
    private String sessionId;
    
    @JsonProperty("properties")
    private Map<String, Object> properties;
    
    @JsonProperty("correlation_id")
    private String correlationId;
}

