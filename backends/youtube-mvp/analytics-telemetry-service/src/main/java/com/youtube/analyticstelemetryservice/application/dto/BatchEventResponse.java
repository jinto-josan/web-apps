package com.youtube.analyticstelemetryservice.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;

/**
 * DTO for batch event responses.
 */
@Data
@Builder
public class BatchEventResponse {
    
    @JsonProperty("total_received")
    private int totalReceived;
    
    @JsonProperty("total_accepted")
    private int totalAccepted;
    
    @JsonProperty("total_rejected")
    private int totalRejected;
    
    @JsonProperty("processed_at")
    private Instant processedAt;
    
    @JsonProperty("results")
    private List<TelemetryEventResponse> results;
}

