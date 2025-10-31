package com.youtube.edgecdncontrol.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.youtube.edgecdncontrol.domain.valueobjects.RuleType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.util.List;
import java.util.Map;

@Data
public class CreateCdnRuleRequest {
    @NotBlank(message = "Rule name is required")
    private String name;
    
    private String description;
    
    @NotNull(message = "Rule type is required")
    private RuleType ruleType;
    
    @NotBlank(message = "Resource group is required")
    private String resourceGroup;
    
    @NotBlank(message = "Front Door profile name is required")
    private String frontDoorProfileName;
    
    private Integer priority;
    
    @NotEmpty(message = "At least one match condition is required")
    @Valid
    private List<MatchConditionDto> matchConditions;
    
    @NotNull(message = "Rule action is required")
    @Valid
    private RuleActionDto action;
    
    private Map<String, Object> metadata;
    
    @Data
    public static class MatchConditionDto {
        @NotNull(message = "Match type is required")
        private RuleMatchConditionType matchType;
        
        @NotBlank(message = "Variable is required")
        private String variable;
        
        @NotBlank(message = "Operator is required")
        private String operator;
        
        @NotEmpty(message = "At least one value is required")
        private List<String> values;
        
        private Boolean caseSensitive = false;
        
        public enum RuleMatchConditionType {
            REQUEST_URI,
            REQUEST_METHOD,
            REQUEST_HEADER,
            QUERY_STRING,
            REQUEST_SCHEME,
            REMOTE_ADDRESS,
            POST_ARGS,
            COOKIE,
            REQUEST_BODY
        }
    }
    
    @Data
    public static class RuleActionDto {
        @NotNull(message = "Action type is required")
        private ActionTypeDto actionType;
        
        private Map<String, String> parameters;
        
        public enum ActionTypeDto {
            ROUTE_TO_ORIGIN,
            REDIRECT,
            REWRITE_URL,
            MODIFY_HEADER,
            CACHE_BYPASS,
            SET_CACHE_DURATION,
            COMPRESS,
            APPLY_WAF_POLICY,
            RATE_LIMIT
        }
    }
}

