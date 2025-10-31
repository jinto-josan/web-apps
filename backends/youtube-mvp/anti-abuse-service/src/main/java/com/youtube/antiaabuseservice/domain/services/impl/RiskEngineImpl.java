package com.youtube.antiaabuseservice.domain.services.impl;

import com.youtube.antiaabuseservice.domain.model.RiskEvent;
import com.youtube.antiaabuseservice.domain.model.RiskScore;
import com.youtube.antiaabuseservice.domain.model.Rule;
import com.youtube.antiaabuseservice.domain.services.MlEndpointClient;
import com.youtube.antiaabuseservice.domain.services.RiskEngine;
import com.youtube.antiaabuseservice.domain.services.RuleEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RiskEngineImpl implements RiskEngine {
    private final MlEndpointClient mlEndpointClient;
    private final RuleEvaluator ruleEvaluator;

    @Override
    public RiskScore calculateRisk(RiskEvent event, Map<String, Object> features, List<Rule> rules) {
        // Evaluate rules first
        List<String> triggeredRules = ruleEvaluator.evaluateRules(rules, features);
        
        // Get ML prediction
        Map<String, Object> mlPredictions;
        try {
            mlPredictions = mlEndpointClient.predict(features);
        } catch (Exception e) {
            log.warn("ML endpoint call failed, using rule-based score only", e);
            mlPredictions = Map.of("risk_score", 0.0);
        }
        
        // Combine rule-based and ML-based scores
        double mlScore = ((Number) mlPredictions.getOrDefault("risk_score", 0.0)).doubleValue();
        double ruleScore = calculateRuleBasedScore(rules, triggeredRules);
        
        // Weighted combination: 70% ML, 30% rules
        double finalScore = 0.7 * mlScore + 0.3 * ruleScore;
        
        // Determine risk level
        RiskScore.RiskLevel riskLevel = determineRiskLevel(finalScore);
        
        // Determine action
        RiskScore.EnforcementAction action = determineAction(rules, triggeredRules, finalScore);
        
        return RiskScore.builder()
                .eventId(event.getId())
                .userId(event.getUserId())
                .score(finalScore)
                .riskLevel(riskLevel)
                .triggeredRules(triggeredRules)
                .features(features)
                .mlPredictions(mlPredictions)
                .recommendedAction(action)
                .timestamp(Instant.now())
                .build();
    }

    private double calculateRuleBasedScore(List<Rule> rules, List<String> triggeredRuleIds) {
        if (triggeredRuleIds.isEmpty()) {
            return 0.0;
        }
        
        return triggeredRuleIds.stream()
                .map(ruleId -> rules.stream()
                        .filter(r -> r.getId().equals(ruleId))
                        .findFirst())
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .mapToDouble(rule -> {
                    // Higher priority rules contribute more
                    return 1.0 - (rule.getPriority() / 100.0);
                })
                .average()
                .orElse(0.0);
    }

    private RiskScore.RiskLevel determineRiskLevel(double score) {
        if (score >= 0.8) {
            return RiskScore.RiskLevel.CRITICAL;
        } else if (score >= 0.6) {
            return RiskScore.RiskLevel.HIGH;
        } else if (score >= 0.4) {
            return RiskScore.RiskLevel.MEDIUM;
        } else {
            return RiskScore.RiskLevel.LOW;
        }
    }

    private RiskScore.EnforcementAction determineAction(List<Rule> rules, List<String> triggeredRuleIds, double score) {
        if (triggeredRuleIds.isEmpty()) {
            return RiskScore.EnforcementAction.ALLOW;
        }
        
        // Get highest priority triggered rule
        return triggeredRuleIds.stream()
                .map(ruleId -> rules.stream()
                        .filter(r -> r.getId().equals(ruleId))
                        .findFirst())
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .max(java.util.Comparator.comparing(Rule::getPriority))
                .map(Rule::getAction)
                .orElse(RiskScore.EnforcementAction.ALLOW);
    }
}

