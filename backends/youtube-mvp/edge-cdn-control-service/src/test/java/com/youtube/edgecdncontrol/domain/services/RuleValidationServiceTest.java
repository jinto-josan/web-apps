package com.youtube.edgecdncontrol.domain.services;

import com.youtube.edgecdncontrol.domain.entities.CdnRule;
import com.youtube.edgecdncontrol.domain.valueobjects.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class RuleValidationServiceTest {
    
    private RuleValidationService validationService;
    
    @BeforeEach
    void setUp() {
        validationService = new RuleValidationService();
    }
    
    @Test
    void shouldValidateValidRule() {
        CdnRule rule = createValidRule();
        RuleValidationService.ValidationResult result = validationService.validate(rule);
        
        assertTrue(result.isValid());
        assertFalse(result.hasErrors());
    }
    
    @Test
    void shouldRejectRuleWithoutName() {
        CdnRule rule = createValidRule();
        CdnRule invalidRule = CdnRule.builder()
                .id(rule.getId())
                .name(null)
                .description(rule.getDescription())
                .ruleType(rule.getRuleType())
                .status(rule.getStatus())
                .frontDoorProfile(rule.getFrontDoorProfile())
                .priority(rule.getPriority())
                .matchConditions(rule.getMatchConditions())
                .action(rule.getAction())
                .metadata(rule.getMetadata())
                .createdBy(rule.getCreatedBy())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .version(rule.getVersion())
                .rollbackFromRuleId(rule.getRollbackFromRuleId())
                .build();
        
        RuleValidationService.ValidationResult result = validationService.validate(invalidRule);
        
        assertFalse(result.isValid());
        assertTrue(result.errors().contains("Rule name is required"));
    }
    
    @Test
    void shouldRejectRuleWithoutMatchConditions() {
        CdnRule rule = createValidRule();
        CdnRule invalidRule = CdnRule.builder()
                .id(rule.getId())
                .name(rule.getName())
                .description(rule.getDescription())
                .ruleType(rule.getRuleType())
                .status(rule.getStatus())
                .frontDoorProfile(rule.getFrontDoorProfile())
                .priority(rule.getPriority())
                .matchConditions(null)
                .action(rule.getAction())
                .metadata(rule.getMetadata())
                .createdBy(rule.getCreatedBy())
                .createdAt(rule.getCreatedAt())
                .updatedAt(rule.getUpdatedAt())
                .version(rule.getVersion())
                .rollbackFromRuleId(rule.getRollbackFromRuleId())
                .build();
        
        RuleValidationService.ValidationResult result = validationService.validate(invalidRule);
        
        assertFalse(result.isValid());
        assertTrue(result.errors().contains("At least one match condition is required"));
    }
    
    private CdnRule createValidRule() {
        RuleMatchCondition condition = RuleMatchCondition.builder()
                .matchType(RuleMatchCondition.MatchType.REQUEST_URI)
                .variable("requestUri")
                .operator("Contains")
                .values(List.of("/api/"))
                .caseSensitive(false)
                .build();
        
        RuleAction action = RuleAction.builder()
                .actionType(RuleAction.ActionType.ROUTE_TO_ORIGIN)
                .parameters(Map.of("originName", "primary-origin"))
                .build();
        
        return CdnRule.builder()
                .id(CdnRuleId.generate())
                .name("Test Rule")
                .description("Test description")
                .ruleType(RuleType.ROUTING_RULE)
                .status(RuleStatus.DRAFT)
                .frontDoorProfile(new FrontDoorProfileId("rg-test", "fd-test"))
                .priority(1)
                .matchConditions(List.of(condition))
                .action(action)
                .metadata(Map.of())
                .createdBy("test-user")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .version("v1")
                .rollbackFromRuleId(Optional.empty())
                .build();
    }
}

