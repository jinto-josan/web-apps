package com.youtube.mvp.streaming.domain.model;

import lombok.*;

/**
 * Policy evaluation result.
 */
@Getter
@Builder
@ToString
public class PolicyResult {
    private boolean allowed;
    private String reason;
    private String policyName;
    
    public static PolicyResult allowed(String policyName) {
        return PolicyResult.builder()
                .allowed(true)
                .policyName(policyName)
                .build();
    }
    
    public static PolicyResult denied(String policyName, String reason) {
        return PolicyResult.builder()
                .allowed(false)
                .policyName(policyName)
                .reason(reason)
                .build();
    }
}

