package com.youtube.configsecretsservice.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Value;

/**
 * Request DTO for secret rotation.
 */
@Value
@Builder
public class SecretRotationRequest {
    @JsonProperty("dry-run")
    Boolean dryRun;
}

