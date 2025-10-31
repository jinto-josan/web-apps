package com.youtube.analyticstelemetryservice.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

/**
 * DTO for telemetry event responses.
 */
@Data
@Builder
public class TelemetryEventResponse {
    
    @JsonProperty("event_id")
    private String eventId;
    
    @JsonProperty("status")
    private String status; // "accepted", "rejected"
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("processed_at")
    private Instant processedAt;
}

