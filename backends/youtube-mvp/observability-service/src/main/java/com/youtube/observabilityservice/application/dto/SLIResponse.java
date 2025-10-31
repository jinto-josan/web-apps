package com.youtube.observabilityservice.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class SLIResponse {
    private String name;
    private String type;
    private String query;
    private Instant lastCalculatedAt;
    private Double lastValue;
}

