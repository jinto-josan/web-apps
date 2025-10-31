package com.youtube.antiaabuseservice.application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskScoreRequest {
    @NotBlank
    private String eventType;
    
    @NotBlank
    private String userId;
    
    private String contentId;
    
    private Map<String, Object> context;
}

