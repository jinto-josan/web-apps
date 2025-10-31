package com.youtube.edgecdncontrol.infrastructure.adapters.persistence;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.edgecdncontrol.domain.entities.CdnRule;
import com.youtube.edgecdncontrol.domain.valueobjects.*;
import com.youtube.edgecdncontrol.infrastructure.adapters.persistence.entity.CdnRuleEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CdnRuleEntityMapper {
    
    private final ObjectMapper objectMapper;
    
    public CdnRuleEntity toEntity(CdnRule rule) {
        try {
            return CdnRuleEntity.builder()
                    .id(rule.getId().getValue())
                    .name(rule.getName())
                    .description(rule.getDescription())
                    .ruleType(rule.getRuleType())
                    .status(rule.getStatus())
                    .resourceGroup(rule.getFrontDoorProfile().getResourceGroup())
                    .profileName(rule.getFrontDoorProfile().getProfileName())
                    .priority(rule.getPriority())
                    .matchConditions(objectMapper.writeValueAsString(rule.getMatchConditions()))
                    .action(objectMapper.writeValueAsString(rule.getAction()))
                    .metadata(rule.getMetadata() != null ? objectMapper.writeValueAsString(rule.getMetadata()) : null)
                    .createdBy(rule.getCreatedBy())
                    .createdAt(rule.getCreatedAt())
                    .updatedAt(rule.getUpdatedAt())
                    .version(rule.getVersion())
                    .rollbackFromRuleId(rule.getRollbackFromRuleId().map(CdnRuleId::getValue).orElse(null))
                    .build();
        } catch (Exception e) {
            log.error("Error mapping CdnRule to entity", e);
            throw new RuntimeException("Failed to map CdnRule to entity", e);
        }
    }
    
    public CdnRule toDomain(CdnRuleEntity entity) {
        try {
            List<RuleMatchCondition> matchConditions = objectMapper.readValue(
                    entity.getMatchConditions(),
                    new TypeReference<List<RuleMatchCondition>>() {}
            );
            
            RuleAction action = objectMapper.readValue(
                    entity.getAction(),
                    RuleAction.class
            );
            
            Map<String, Object> metadata = entity.getMetadata() != null ?
                    objectMapper.readValue(entity.getMetadata(), new TypeReference<Map<String, Object>>() {}) :
                    null;
            
            return CdnRule.builder()
                    .id(CdnRuleId.of(entity.getId()))
                    .name(entity.getName())
                    .description(entity.getDescription())
                    .ruleType(entity.getRuleType())
                    .status(entity.getStatus())
                    .frontDoorProfile(new FrontDoorProfileId(entity.getResourceGroup(), entity.getProfileName()))
                    .priority(entity.getPriority())
                    .matchConditions(matchConditions)
                    .action(action)
                    .metadata(metadata)
                    .createdBy(entity.getCreatedBy())
                    .createdAt(entity.getCreatedAt())
                    .updatedAt(entity.getUpdatedAt())
                    .version(entity.getVersion())
                    .rollbackFromRuleId(entity.getRollbackFromRuleId() != null ?
                            Optional.of(CdnRuleId.of(entity.getRollbackFromRuleId())) :
                            Optional.empty())
                    .build();
        } catch (Exception e) {
            log.error("Error mapping entity to CdnRule", e);
            throw new RuntimeException("Failed to map entity to CdnRule", e);
        }
    }
}

