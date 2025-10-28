package com.youtube.recommendationsservice.domain.valueobjects;

import lombok.Value;

@Value
public class VideoId {
    String value;

    private VideoId(String value) {
        this.value = value;
    }

    public static VideoId from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Video ID cannot be null or blank");
        }
        return new VideoId(value);
    }
}

