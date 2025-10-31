package com.youtube.observabilityservice.domain.entities;

import com.youtube.observabilityservice.domain.valueobjects.SLOId;
import com.youtube.observabilityservice.domain.valueobjects.TimeWindow;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@Builder
public class SLO {
    private SLOId id;
    private String name;
    private String serviceName;
    private String description;
    private List<SLI> slis;
    private double targetPercent; // e.g., 99.9 for 99.9%
    private TimeWindow timeWindow; // Rolling 30 days, etc.
    private double errorBudget; // Calculated
    private double errorBudgetRemaining; // Calculated
    private Map<String, String> labels; // Additional metadata
    private Instant createdAt;
    private Instant updatedAt;

    public double calculateErrorBudget() {
        return 100.0 - targetPercent;
    }

    public double calculateRemainingErrorBudget(double currentSLI) {
        return Math.max(0, currentSLI - targetPercent);
    }
}

