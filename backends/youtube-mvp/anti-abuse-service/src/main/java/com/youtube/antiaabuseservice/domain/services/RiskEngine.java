package com.youtube.antiaabuseservice.domain.services;

import com.youtube.antiaabuseservice.domain.model.RiskEvent;
import com.youtube.antiaabuseservice.domain.model.RiskScore;
import com.youtube.antiaabuseservice.domain.model.Rule;

import java.util.List;
import java.util.Map;

public interface RiskEngine {
    /**
     * Calculate risk score for an event using ML model and rules.
     */
    RiskScore calculateRisk(RiskEvent event, Map<String, Object> features, List<Rule> rules);
}

