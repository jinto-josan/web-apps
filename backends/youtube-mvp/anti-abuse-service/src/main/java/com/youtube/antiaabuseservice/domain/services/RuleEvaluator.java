package com.youtube.antiaabuseservice.domain.services;

import com.youtube.antiaabuseservice.domain.model.RiskScore;
import com.youtube.antiaabuseservice.domain.model.Rule;

import java.util.List;
import java.util.Map;

public interface RuleEvaluator {
    /**
     * Evaluate rules against features and return triggered rules.
     */
    List<String> evaluateRules(List<Rule> rules, Map<String, Object> features);
}

