package com.youtube.edgecdncontrol.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.youtube.edgecdncontrol.domain.valueobjects.RuleStatus;
import com.youtube.edgecdncontrol.domain.valueobjects.RuleType;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Data
@Builder
public class CdnRuleResponse {
    private String id;
    private String name;
    private String description;
    private RuleType ruleType;
    private RuleStatus status;
    private String resourceGroup;
    private String frontDoorProfileName;
    private Integer priority;
    private List<MatchConditionDto> matchConditions;
    private RuleActionDto action;
    private Map<String, Object> metadata;
    private String createdBy;
    private Instant createdAt;
    private Instant updatedAt;
    private String version; // ETag
    private Optional<String> rollbackFromRuleId;
    
    @Data
    @Builder
    public static class MatchConditionDto {
        private RuleMatchConditionType matchType;
        private String variable;
        private String operator;
        private List<String> values;
        private Boolean caseSensitive;
        
        public enum RuleMatchConditionType {
            REQUEST_URI, REQUEST_METHOD, REQUEST_HEADER, QUERY_STRING,
            REQUEST_SCHEME, REMOTE_ADDRESS, POST_ARGS, COOKIE, REQUEST_BODY
        }
    }
    
    @Data
    @Builder
    public static class RuleActionDto {
        private ActionTypeDto actionType;
        private Map<String, String> parameters;
        
        public enum ActionTypeDto {
            ROUTE_TO_ORIGIN, REDIRECT, REWRITE_URL, MODIFY_HEADER,
            CACHE_BYPASS, SET_CACHE_DURATION, COMPRESS, APPLY_WAF_POLICY, RATE_LIMIT
        }
    }
}

