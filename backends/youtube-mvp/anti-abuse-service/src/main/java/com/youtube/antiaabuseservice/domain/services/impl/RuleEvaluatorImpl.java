package com.youtube.antiaabuseservice.domain.services.impl;

import com.youtube.antiaabuseservice.domain.model.Rule;
import com.youtube.antiaabuseservice.domain.services.RuleEvaluator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class RuleEvaluatorImpl implements RuleEvaluator {

    @Override
    public List<String> evaluateRules(List<Rule> rules, Map<String, Object> features) {
        List<String> triggeredRules = new ArrayList<>();
        
        for (Rule rule : rules) {
            if (!rule.isEnabled()) {
                continue;
            }
            
            if (evaluateCondition(rule.getCondition(), features)) {
                triggeredRules.add(rule.getId());
            }
        }
        
        return triggeredRules;
    }

    private boolean evaluateCondition(Rule.RuleCondition condition, Map<String, Object> features) {
        if (condition.getPredicates().isEmpty()) {
            return false;
        }
        
        if ("AND".equals(condition.getOperator())) {
            return condition.getPredicates().stream()
                    .allMatch(predicate -> evaluatePredicate(predicate, features));
        } else { // OR
            return condition.getPredicates().stream()
                    .anyMatch(predicate -> evaluatePredicate(predicate, features));
        }
    }

    private boolean evaluatePredicate(Rule.RulePredicate predicate, Map<String, Object> features) {
        Object featureValue = features.get(predicate.getField());
        if (featureValue == null) {
            return false;
        }
        
        String operator = predicate.getOperator();
        Object ruleValue = predicate.getValue();
        
        try {
            switch (operator) {
                case "GT":
                    return compareNumbers(featureValue, ruleValue) > 0;
                case "LT":
                    return compareNumbers(featureValue, ruleValue) < 0;
                case "EQ":
                    return featureValue.equals(ruleValue);
                case "NOT_EQ":
                    return !featureValue.equals(ruleValue);
                case "IN":
                    if (ruleValue instanceof List) {
                        return ((List<?>) ruleValue).contains(featureValue);
                    }
                    return false;
                case "NOT_IN":
                    if (ruleValue instanceof List) {
                        return !((List<?>) ruleValue).contains(featureValue);
                    }
                    return false;
                default:
                    log.warn("Unknown operator: {}", operator);
                    return false;
            }
        } catch (Exception e) {
            log.warn("Error evaluating predicate: {}", predicate, e);
            return false;
        }
    }

    @SuppressWarnings("unchecked")
    private int compareNumbers(Object value1, Object value2) {
        if (value1 instanceof Number && value2 instanceof Number) {
            return Double.compare(((Number) value1).doubleValue(), ((Number) value2).doubleValue());
        }
        return 0;
    }
}

