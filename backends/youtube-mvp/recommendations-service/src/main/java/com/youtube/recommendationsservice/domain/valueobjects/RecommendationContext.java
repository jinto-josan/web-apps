package com.youtube.recommendationsservice.domain.valueobjects;

import lombok.Builder;
import lombok.Value;

import java.util.Map;
import java.util.Optional;

@Value
@Builder
public class RecommendationContext {
    String device;
    String location;
    String language;
    String abTestVariant;
    Map<String, String> customAttributes;

    public Optional<String> getCustomAttribute(String key) {
        return Optional.ofNullable(customAttributes)
            .flatMap(attrs -> Optional.ofNullable(attrs.get(key)));
    }

    public static RecommendationContext defaultContext() {
        return RecommendationContext.builder()
            .device("web")
            .location("us")
            .language("en")
            .abTestVariant("control")
            .build();
    }
}

