package com.youtube.observabilityservice.domain.entities;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Getter
@Setter
@Builder
public class SLI {
    private String name;
    private SLIType type;
    private String query; // KQL query for Azure Monitor
    private Instant lastCalculatedAt;
    private Double lastValue; // Last calculated SLI value (0-100)

    public enum SLIType {
        AVAILABILITY,
        LATENCY,
        ERROR_RATE,
        THROUGHPUT,
        CUSTOM
    }
}

