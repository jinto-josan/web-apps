package com.youtube.recommendationsservice.domain.valueobjects;

import lombok.Value;

import java.util.UUID;

@Value
public class RecommendationId {
    String value;

    private RecommendationId(String value) {
        this.value = value;
    }

    public static RecommendationId generate() {
        return new RecommendationId(UUID.randomUUID().toString());
    }

    public static RecommendationId from(String value) {
        return new RecommendationId(value);
    }
}

