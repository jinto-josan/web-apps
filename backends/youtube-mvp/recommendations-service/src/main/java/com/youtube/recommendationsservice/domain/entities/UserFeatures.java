package com.youtube.recommendationsservice.domain.entities;

import com.youtube.recommendationsservice.domain.valueobjects.FeatureVector;
import com.youtube.recommendationsservice.domain.valueobjects.UserId;
import lombok.Builder;
import lombok.Value;

import java.time.Instant;
import java.util.List;

@Value
@Builder
public class UserFeatures {
    UserId userId;
    FeatureVector features;
    List<String> recentlyViewedCategories;
    List<String> preferredLanguages;
    Instant lastUpdated;

    public static UserFeatures of(UserId userId, FeatureVector features) {
        return UserFeatures.builder()
            .userId(userId)
            .features(features)
            .recentlyViewedCategories(List.of())
            .preferredLanguages(List.of("en"))
            .lastUpdated(Instant.now())
            .build();
    }
}

