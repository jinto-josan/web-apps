package com.youtube.observabilityservice.domain.entities;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Builder
public class SyntheticCheckResult {
    private Instant executedAt;
    private Boolean success;
    private Integer statusCode;
    private Long responseTimeMs;
    private String responseBody;
    private String errorMessage;
    private Map<String, String> metadata; // Additional context
}

