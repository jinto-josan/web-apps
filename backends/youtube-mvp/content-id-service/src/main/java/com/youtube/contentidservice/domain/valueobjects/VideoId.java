package com.youtube.contentidservice.domain.valueobjects;

import lombok.Value;
import java.util.UUID;

@Value
public class VideoId {
    UUID value;

    public static VideoId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("VideoId cannot be null");
        }
        return new VideoId(value);
    }

    public static VideoId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("VideoId cannot be null or blank");
        }
        return new VideoId(UUID.fromString(value));
    }
}

