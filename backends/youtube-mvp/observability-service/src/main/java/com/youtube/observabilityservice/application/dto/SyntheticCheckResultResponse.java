package com.youtube.observabilityservice.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
public class SyntheticCheckResultResponse {
    private Instant executedAt;
    private Boolean success;
    private Integer statusCode;
    private Long responseTimeMs;
    private String errorMessage;
    private Map<String, String> metadata;
}

