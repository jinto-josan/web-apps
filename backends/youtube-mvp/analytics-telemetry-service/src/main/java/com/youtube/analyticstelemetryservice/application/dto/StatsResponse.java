package com.youtube.analyticstelemetryservice.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

/**
 * DTO for service statistics.
 */
@Data
@Builder
public class StatsResponse {
    
    @JsonProperty("total_events_processed")
    private long totalEventsProcessed;
    
    @JsonProperty("events_per_second")
    private double eventsPerSecond;
    
    @JsonProperty("publisher_healthy")
    private boolean publisherHealthy;
    
    @JsonProperty("backpressure_active")
    private boolean backpressureActive;
    
    @JsonProperty("events_by_type")
    private Map<String, Long> eventsByType;
    
    @JsonProperty("events_by_source")
    private Map<String, Long> eventsBySource;
    
    @JsonProperty("errors_count")
    private long errorsCount;
    
    @JsonProperty("dlq_count")
    private long dlqCount;
    
    @JsonProperty("last_updated")
    private Instant lastUpdated;
}

