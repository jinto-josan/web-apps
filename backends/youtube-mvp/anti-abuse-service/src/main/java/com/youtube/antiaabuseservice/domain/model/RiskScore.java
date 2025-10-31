package com.youtube.antiaabuseservice.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskScore {
    private String eventId;
    private String userId;
    private Double score; // 0.0 to 1.0
    private RiskLevel riskLevel;
    private List<String> triggeredRules;
    private Map<String, Object> features;
    private Map<String, Object> mlPredictions;
    private EnforcementAction recommendedAction;
    private Instant timestamp;
    private Long latencyMs;

    public enum RiskLevel {
        LOW, MEDIUM, HIGH, CRITICAL
    }

    public enum EnforcementAction {
        ALLOW, WARN, REVIEW, BLOCK, ESCALATE
    }
}

