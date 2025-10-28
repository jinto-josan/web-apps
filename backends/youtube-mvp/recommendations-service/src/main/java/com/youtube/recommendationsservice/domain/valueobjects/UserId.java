package com.youtube.recommendationsservice.domain.valueobjects;

import lombok.Value;

@Value
public class UserId {
    String value;

    private UserId(String value) {
        this.value = value;
    }

    public static UserId from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or blank");
        }
        return new UserId(value);
    }
}

