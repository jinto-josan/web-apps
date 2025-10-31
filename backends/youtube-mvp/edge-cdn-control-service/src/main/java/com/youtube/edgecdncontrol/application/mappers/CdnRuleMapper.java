package com.youtube.edgecdncontrol.application.mappers;

import com.youtube.edgecdncontrol.application.dto.CdnRuleResponse;
import com.youtube.edgecdncontrol.application.dto.CreateCdnRuleRequest;
import com.youtube.edgecdncontrol.domain.entities.CdnRule;
import com.youtube.edgecdncontrol.domain.valueobjects.*;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

import java.util.Optional;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface CdnRuleMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "DRAFT")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "rollbackFromRuleId", ignore = true)
    CdnRule toDomain(CreateCdnRuleRequest request);
    
    @Mapping(source = "id.value", target = "id")
    @Mapping(source = "frontDoorProfile.resourceGroup", target = "resourceGroup")
    @Mapping(source = "frontDoorProfile.profileName", target = "frontDoorProfileName")
    @Mapping(source = "rollbackFromRuleId", target = "rollbackFromRuleId", 
             expression = "java(rule.getRollbackFromRuleId().map(r -> r.getValue()).orElse(null))")
    CdnRuleResponse toResponse(CdnRule rule);
    
    default RuleMatchCondition toMatchCondition(CreateCdnRuleRequest.MatchConditionDto dto) {
        return RuleMatchCondition.builder()
                .matchType(mapMatchType(dto.getMatchType()))
                .variable(dto.getVariable())
                .operator(dto.getOperator())
                .values(dto.getValues())
                .caseSensitive(dto.getCaseSensitive() != null ? dto.getCaseSensitive() : false)
                .build();
    }
    
    default CdnRuleResponse.MatchConditionDto toMatchConditionDto(RuleMatchCondition condition) {
        return CdnRuleResponse.MatchConditionDto.builder()
                .matchType(mapMatchType(condition.getMatchType()))
                .variable(condition.getVariable())
                .operator(condition.getOperator())
                .values(condition.getValues())
                .caseSensitive(condition.isCaseSensitive())
                .build();
    }
    
    default RuleAction toRuleAction(CreateCdnRuleRequest.RuleActionDto dto) {
        return RuleAction.builder()
                .actionType(mapActionType(dto.getActionType()))
                .parameters(dto.getParameters())
                .build();
    }
    
    default CdnRuleResponse.RuleActionDto toRuleActionDto(RuleAction action) {
        return CdnRuleResponse.RuleActionDto.builder()
                .actionType(mapActionTypeDto(action.getActionType()))
                .parameters(action.getParameters())
                .build();
    }
    
    default FrontDoorProfileId toFrontDoorProfileId(String resourceGroup, String profileName) {
        return new FrontDoorProfileId(resourceGroup, profileName);
    }
    
    RuleMatchCondition.MatchType mapMatchType(CreateCdnRuleRequest.MatchConditionDto.RuleMatchConditionType type);
    CdnRuleResponse.MatchConditionDto.RuleMatchConditionType mapMatchType(RuleMatchCondition.MatchType type);
    
    RuleAction.ActionType mapActionType(CreateCdnRuleRequest.RuleActionDto.ActionTypeDto type);
    CdnRuleResponse.RuleActionDto.ActionTypeDto mapActionTypeDto(RuleAction.ActionType type);
}

