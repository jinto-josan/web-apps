package com.youtube.configsecretsservice.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;

/**
 * Response DTO for configuration operations.
 */
@Value
@Builder
public class ConfigResponse {
    String key;
    String scope;
    String value;
    
    @JsonProperty("content-type")
    String contentType;
    
    String label;
    String etag;
    
    @JsonProperty("created-at")
    Instant createdAt;
    
    @JsonProperty("updated-at")
    Instant updatedAt;
    
    @JsonProperty("created-by")
    String createdBy;
    
    @JsonProperty("updated-by")
    String updatedBy;
}

