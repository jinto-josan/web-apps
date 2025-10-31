package com.youtube.configsecretsservice.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.UUID;

/**
 * Response DTO for secret rotation.
 */
@Value
@Builder
public class SecretRotationResponse {
    UUID id;
    String scope;
    String key;
    String status;
    
    @JsonProperty("scheduled-at")
    Instant scheduledAt;
    
    @JsonProperty("completed-at")
    Instant completedAt;
    
    @JsonProperty("triggered-by")
    String triggeredBy;
    
    @JsonProperty("dry-run")
    Boolean dryRun;
}

