package com.youtube.edgecdncontrol.domain.entities;

import com.youtube.edgecdncontrol.domain.valueobjects.*;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Value
@Builder
public class CdnRule {
    CdnRuleId id;
    String name;
    String description;
    RuleType ruleType;
    RuleStatus status;
    FrontDoorProfileId frontDoorProfile;
    Integer priority;
    List<RuleMatchCondition> matchConditions;
    RuleAction action;
    Map<String, Object> metadata;
    String createdBy;
    Instant createdAt;
    Instant updatedAt;
    String version; // ETag for optimistic locking
    Optional<CdnRuleId> rollbackFromRuleId;

    public CdnRule validate() {
        if (name == null || name.isBlank()) {
            throw new IllegalStateException("Rule name is required");
        }
        if (matchConditions == null || matchConditions.isEmpty()) {
            throw new IllegalStateException("At least one match condition is required");
        }
        if (action == null) {
            throw new IllegalStateException("Rule action is required");
        }
        if (priority != null && priority < 0) {
            throw new IllegalStateException("Priority must be non-negative");
        }
        return this;
    }

    public CdnRule markAsValidated() {
        return CdnRule.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .ruleType(this.ruleType)
                .status(RuleStatus.VALIDATED)
                .frontDoorProfile(this.frontDoorProfile)
                .priority(this.priority)
                .matchConditions(this.matchConditions)
                .action(this.action)
                .metadata(this.metadata)
                .createdBy(this.createdBy)
                .createdAt(this.createdAt)
                .updatedAt(Instant.now())
                .version(this.version)
                .rollbackFromRuleId(this.rollbackFromRuleId)
                .build();
    }

    public CdnRule markAsApplied() {
        return CdnRule.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .ruleType(this.ruleType)
                .status(RuleStatus.APPLIED)
                .frontDoorProfile(this.frontDoorProfile)
                .priority(this.priority)
                .matchConditions(this.matchConditions)
                .action(this.action)
                .metadata(this.metadata)
                .createdBy(this.createdBy)
                .createdAt(this.createdAt)
                .updatedAt(Instant.now())
                .version(this.version)
                .rollbackFromRuleId(this.rollbackFromRuleId)
                .build();
    }

    public CdnRule markAsFailed(String errorMessage) {
        Map<String, Object> updatedMetadata = Map.of(
                "error", errorMessage,
                "failedAt", Instant.now().toString()
        );
        return CdnRule.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .ruleType(this.ruleType)
                .status(RuleStatus.FAILED)
                .frontDoorProfile(this.frontDoorProfile)
                .priority(this.priority)
                .matchConditions(this.matchConditions)
                .action(this.action)
                .metadata(updatedMetadata)
                .createdBy(this.createdBy)
                .createdAt(this.createdAt)
                .updatedAt(Instant.now())
                .version(this.version)
                .rollbackFromRuleId(this.rollbackFromRuleId)
                .build();
    }

    public CdnRule markDriftDetected() {
        return CdnRule.builder()
                .id(this.id)
                .name(this.name)
                .description(this.description)
                .ruleType(this.ruleType)
                .status(RuleStatus.DRIFT_DETECTED)
                .frontDoorProfile(this.frontDoorProfile)
                .priority(this.priority)
                .matchConditions(this.matchConditions)
                .action(this.action)
                .metadata(this.metadata)
                .createdBy(this.createdBy)
                .createdAt(this.createdAt)
                .updatedAt(Instant.now())
                .version(this.version)
                .rollbackFromRuleId(this.rollbackFromRuleId)
                .build();
    }
}

