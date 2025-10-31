package com.youtube.antiaabuseservice.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RiskScoreResponse {
    private String eventId;
    private Double score;
    private String riskLevel;
    private List<String> triggeredRules;
    private String recommendedAction;
    private Long latencyMs;
    private Map<String, Object> metadata;
}

