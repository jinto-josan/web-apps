package com.youtube.recommendationsservice.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecommendationResponse {
    private List<RecommendedVideoDto> recommendations;
    private Metadata metadata;
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class RecommendedVideoDto {
        private String videoId;
        private String title;
        private String category;
        private Double score;
        private String reason;
        private Instant recommendedAt;
        private Map<String, Object> additionalInfo;
    }
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Metadata {
        private String userId;
        private String requestType;
        private Instant timestamp;
        private String abTestVariant;
        private Integer totalCandidates;
        private Integer totalReturned;
    }
}

