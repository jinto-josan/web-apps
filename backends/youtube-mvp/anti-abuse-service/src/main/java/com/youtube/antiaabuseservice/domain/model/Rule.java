package com.youtube.antiaabuseservice.domain.model;

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
public class Rule {
    private String id;
    private String name;
    private String description;
    private RuleCondition condition;
    private EnforcementAction action;
    private Integer priority;
    private boolean enabled;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RuleCondition {
        private String operator; // AND, OR
        private List<RulePredicate> predicates;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RulePredicate {
        private String field;
        private String operator; // GT, LT, EQ, IN, NOT_IN, BETWEEN
        private Object value;
    }

    public enum EnforcementAction {
        ALLOW, WARN, REVIEW, BLOCK, ESCALATE
    }
}

