package com.youtube.edgecdncontrol.domain.valueobjects;

import lombok.Builder;
import lombok.Value;

import java.util.Map;

@Value
@Builder
public class RuleAction {
    ActionType actionType;
    Map<String, String> parameters;

    public enum ActionType {
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

