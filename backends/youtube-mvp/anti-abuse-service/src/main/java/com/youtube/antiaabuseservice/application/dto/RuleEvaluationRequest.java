package com.youtube.antiaabuseservice.application.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RuleEvaluationRequest {
    @NotBlank
    private String userId;
    
    private Map<String, Object> features;
    private Map<String, Object> context;
}

