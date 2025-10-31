package com.youtube.observabilityservice.application.dto;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class SLOResponse {
    private String id;
    private String name;
    private String serviceName;
    private String description;
    private List<SLIResponse> slis;
    private Double targetPercent;
    private String timeWindow;
    private Double currentSLO;
    private Double errorBudget;
    private Double errorBudgetRemaining;
    private Double errorBudgetBurnRate;
    private Map<String, String> labels;
    private Instant createdAt;
    private Instant updatedAt;
}

