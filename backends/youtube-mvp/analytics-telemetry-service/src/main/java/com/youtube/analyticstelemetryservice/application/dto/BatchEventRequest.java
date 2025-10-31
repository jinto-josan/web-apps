package com.youtube.analyticstelemetryservice.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/**
 * DTO for batch event collection requests.
 * Supports up to 1000 events per batch.
 */
@Data
public class BatchEventRequest {
    
    @NotNull(message = "Events list is required")
    @NotEmpty(message = "Events list cannot be empty")
    @Size(max = 1000, message = "Maximum 1000 events per batch")
    @Valid
    @JsonProperty("events")
    private List<TelemetryEventRequest> events;
    
    @JsonProperty("idempotency_key")
    private String idempotencyKey; // Optional - for idempotent processing
}

