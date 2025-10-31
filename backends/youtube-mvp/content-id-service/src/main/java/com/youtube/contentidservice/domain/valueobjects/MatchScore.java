package com.youtube.contentidservice.domain.valueobjects;

import lombok.Value;

@Value
public class MatchScore {
    double value;

    public static MatchScore of(double value) {
        if (value < 0.0 || value > 1.0) {
            throw new IllegalArgumentException("MatchScore must be between 0.0 and 1.0");
        }
        return new MatchScore(value);
    }

    public boolean isStrongMatch() {
        return value >= 0.9;
    }

    public boolean isModerateMatch() {
        return value >= 0.7 && value < 0.9;
    }

    public boolean isWeakMatch() {
        return value >= 0.5 && value < 0.7;
    }
}

