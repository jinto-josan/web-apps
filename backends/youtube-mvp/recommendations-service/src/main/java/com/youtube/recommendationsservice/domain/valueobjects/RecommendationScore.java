package com.youtube.recommendationsservice.domain.valueobjects;

import lombok.Value;

@Value
public class RecommendationScore {
    double value;

    private RecommendationScore(double value) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException("Recommendation score must be between 0.0 and 1.0");
        }
        this.value = value;
    }

    public static RecommendationScore of(double value) {
        return new RecommendationScore(value);
    }

    public static RecommendationScore zero() {
        return new RecommendationScore(0.0);
    }

    public boolean isGreaterThan(RecommendationScore other) {
        return this.value > other.value;
    }
}

