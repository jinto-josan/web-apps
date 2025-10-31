package com.youtube.observabilityservice.domain.entities;

import com.youtube.observabilityservice.domain.valueobjects.SyntheticCheckId;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.Map;

@Getter
@Setter
@Builder
public class SyntheticCheck {
    private SyntheticCheckId id;
    private String name;
    private String description;
    private SyntheticCheckType type;
    private String endpoint;
    private String method; // GET, POST, etc.
    private Map<String, String> headers;
    private String body; // For POST requests
    private Integer expectedStatusCode;
    private String expectedBodyPattern; // Regex pattern
    private Integer timeoutSeconds;
    private Integer intervalSeconds; // How often to run
    private Boolean enabled;
    private Instant lastRunAt;
    private SyntheticCheckResult lastResult;
    private Map<String, String> labels;
    private Instant createdAt;
    private Instant updatedAt;

    public enum SyntheticCheckType {
        HTTP,
        HTTPS,
        TCP,
        DNS,
        SSL_CERT,
        MULTI_STEP
    }
}

