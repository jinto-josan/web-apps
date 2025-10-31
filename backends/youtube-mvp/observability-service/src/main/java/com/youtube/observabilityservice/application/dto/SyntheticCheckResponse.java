package com.youtube.observabilityservice.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.Map;

@Data
@Builder
public class SyntheticCheckResponse {
    private String id;
    private String name;
    private String description;
    private String type;
    private String endpoint;
    private String method;
    private Map<String, String> headers;
    private Integer expectedStatusCode;
    private Integer timeoutSeconds;
    private Integer intervalSeconds;
    private Boolean enabled;
    private Instant lastRunAt;
    private SyntheticCheckResultResponse lastResult;
    private Map<String, String> labels;
    private Instant createdAt;
    private Instant updatedAt;
}

