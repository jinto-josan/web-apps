package com.youtube.antiaabuseservice.domain.services.impl;

import com.youtube.antiaabuseservice.domain.model.Rule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class RuleEvaluatorTest {
    private RuleEvaluatorImpl ruleEvaluator;

    @BeforeEach
    void setUp() {
        ruleEvaluator = new RuleEvaluatorImpl();
    }

    @Test
    void testEvaluateRules_SimpleGT() {
        Rule rule = Rule.builder()
                .id("rule1")
                .name("High Event Count")
                .enabled(true)
                .priority(100)
                .action(Rule.EnforcementAction.BLOCK)
                .condition(Rule.RuleCondition.builder()
                        .operator("AND")
                        .predicates(Arrays.asList(
                                Rule.RulePredicate.builder()
                                        .field("totalEvents")
                                        .operator("GT")
                                        .value(100)
                                        .build()))
                        .build())
                .build();

        Map<String, Object> features = Map.of("totalEvents", 150);

        List<String> triggered = ruleEvaluator.evaluateRules(List.of(rule), features);
        
        assertTrue(triggered.contains("rule1"));
    }

    @Test
    void testEvaluateRules_ANDCondition() {
        Rule rule = Rule.builder()
                .id("rule1")
                .enabled(true)
                .priority(100)
                .action(Rule.EnforcementAction.BLOCK)
                .condition(Rule.RuleCondition.builder()
                        .operator("AND")
                        .predicates(Arrays.asList(
                                Rule.RulePredicate.builder()
                                        .field("totalEvents")
                                        .operator("GT")
                                        .value(100)
                                        .build(),
                                Rule.RulePredicate.builder()
                                        .field("riskHistory")
                                        .operator("GT")
                                        .value(0.5)
                                        .build()))
                        .build())
                .build();

        Map<String, Object> features = Map.of(
                "totalEvents", 150,
                "riskHistory", 0.6
        );

        List<String> triggered = ruleEvaluator.evaluateRules(List.of(rule), features);
        
        assertTrue(triggered.contains("rule1"));
    }

    @Test
    void testEvaluateRules_DisabledRule() {
        Rule rule = Rule.builder()
                .id("rule1")
                .enabled(false)
                .priority(100)
                .action(Rule.EnforcementAction.BLOCK)
                .condition(Rule.RuleCondition.builder()
                        .operator("AND")
                        .predicates(Arrays.asList(
                                Rule.RulePredicate.builder()
                                        .field("totalEvents")
                                        .operator("GT")
                                        .value(100)
                                        .build()))
                        .build())
                .build();

        Map<String, Object> features = Map.of("totalEvents", 150);

        List<String> triggered = ruleEvaluator.evaluateRules(List.of(rule), features);
        
        assertFalse(triggered.contains("rule1"));
    }
}

