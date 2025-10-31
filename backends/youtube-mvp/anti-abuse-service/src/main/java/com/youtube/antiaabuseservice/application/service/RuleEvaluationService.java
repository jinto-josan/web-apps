package com.youtube.antiaabuseservice.application.service;

import com.youtube.antiaabuseservice.application.dto.RuleEvaluationRequest;
import com.youtube.antiaabuseservice.application.dto.RuleEvaluationResponse;
import com.youtube.antiaabuseservice.domain.model.Rule;
import com.youtube.antiaabuseservice.domain.repositories.RuleRepository;
import com.youtube.antiaabuseservice.domain.services.RuleEvaluator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class RuleEvaluationService {
    private final RuleRepository ruleRepository;
    private final RuleEvaluator ruleEvaluator;

    @Transactional(readOnly = true)
    public RuleEvaluationResponse evaluateRules(RuleEvaluationRequest request) {
        List<Rule> rules = ruleRepository.findAllEnabled();
        
        Map<String, Object> allFeatures = new HashMap<>(request.getFeatures() != null ? request.getFeatures() : Map.of());
        allFeatures.putAll(request.getContext() != null ? request.getContext() : Map.of());
        allFeatures.put("userId", request.getUserId());
        
        List<String> triggeredRules = ruleEvaluator.evaluateRules(rules, allFeatures);
        
        // Determine recommended action based on highest priority triggered rule
        String recommendedAction = determineAction(rules, triggeredRules);
        
        return RuleEvaluationResponse.builder()
                .triggeredRules(triggeredRules)
                .recommendedAction(recommendedAction)
                .metadata(Map.of("userId", request.getUserId()))
                .build();
    }

    private String determineAction(List<Rule> rules, List<String> triggeredRuleIds) {
        return triggeredRuleIds.stream()
                .map(ruleId -> rules.stream()
                        .filter(r -> r.getId().equals(ruleId))
                        .findFirst())
                .filter(java.util.Optional::isPresent)
                .map(java.util.Optional::get)
                .max(java.util.Comparator.comparing(Rule::getPriority))
                .map(rule -> rule.getAction().name())
                .orElse("ALLOW");
    }
}

