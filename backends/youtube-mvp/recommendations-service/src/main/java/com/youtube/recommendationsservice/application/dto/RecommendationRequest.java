package com.youtube.recommendationsservice.application.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RecommendationRequest {
    @NotBlank(message = "userId is required")
    private String userId;
    
    private String videoId; // For "next up" recommendations
    
    @NotNull(message = "limit is required")
    @Min(value = 1, message = "limit must be at least 1")
    @Max(value = 100, message = "limit must be at most 100")
    private Integer limit;
    
    private String device;
    private String location;
    private String language;
    private String abTestVariant;
    
    // For fallback
    private Integer fallbackLimit;
}

