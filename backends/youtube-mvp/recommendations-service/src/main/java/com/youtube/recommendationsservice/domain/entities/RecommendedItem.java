package com.youtube.recommendationsservice.domain.entities;

import com.youtube.recommendationsservice.domain.valueobjects.RecommendationScore;
import com.youtube.recommendationsservice.domain.valueobjects.VideoId;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.Map;

@Value
@Builder
public class RecommendedItem {
    VideoId videoId;
    RecommendationScore score;
    String reason;
    Instant recommendedAt;
    Map<String, Object> metadata;

    public static RecommendedItem of(VideoId videoId, RecommendationScore score, String reason) {
        return RecommendedItem.builder()
            .videoId(videoId)
            .score(score)
            .reason(reason)
            .recommendedAt(Instant.now())
            .metadata(Map.of())
            .build();
    }
}

