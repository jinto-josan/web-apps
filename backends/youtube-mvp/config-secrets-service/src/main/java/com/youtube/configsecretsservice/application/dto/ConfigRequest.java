package com.youtube.configsecretsservice.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Value;

/**
 * Request DTO for configuration operations.
 */
@Value
@Builder
public class ConfigRequest {
    @NotBlank(message = "Value is required")
    String value;
    
    @JsonProperty("content-type")
    String contentType;
    
    String label;
    
    @JsonProperty("is-secret")
    Boolean isSecret;
}

