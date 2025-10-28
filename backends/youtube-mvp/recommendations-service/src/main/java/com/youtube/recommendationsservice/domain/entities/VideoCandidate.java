package com.youtube.recommendationsservice.domain.entities;

import com.youtube.recommendationsservice.domain.valueobjects.FeatureVector;
import com.youtube.recommendationsservice.domain.valueobjects.VideoId;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Value
@Builder
public class VideoCandidate {
    VideoId videoId;
    String title;
    String category;
    List<String> tags;
    Instant publishedAt;
    FeatureVector features;
    Map<String, Object> metadata;

    public static VideoCandidate of(VideoId videoId, String title, String category) {
        return VideoCandidate.builder()
            .videoId(videoId)
            .title(title)
            .features(FeatureVector.empty())
            .category(category)
            .tags(List.of())
            .publishedAt(Instant.now())
            .metadata(Map.of())
            .build();
    }
}

