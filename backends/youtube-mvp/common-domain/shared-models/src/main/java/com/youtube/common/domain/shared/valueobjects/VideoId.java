package com.youtube.common.domain.shared.valueobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.youtube.common.domain.core.Identifier;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;
import java.util.UUID;

/**
 * Value object representing a video identifier.
 * Immutable and type-safe.
 * 
 * <p>Used across multiple services:
 * <ul>
 *   <li>video-catalog-service - Video metadata</li>
 *   <li>content-id-service - Content identification</li>
 *   <li>recommendations-service - Video recommendations</li>
 *   <li>streaming-session-service - Video playback</li>
 *   <li>comments-service - Video comments</li>
 * </ul>
 */
public final class VideoId implements Identifier {
    
    @NotBlank
    private final String value;

    @JsonCreator
    private VideoId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Video ID cannot be null or blank");
        }
        this.value = value.trim();
    }

    public static VideoId from(String value) {
        return new VideoId(value);
    }

    public static VideoId of(String value) {
        return from(value);
    }

    public static VideoId from(UUID uuid) {
        if (uuid == null) {
            throw new IllegalArgumentException("Video ID UUID cannot be null");
        }
        return new VideoId(uuid.toString());
    }

    public static VideoId of(UUID uuid) {
        return from(uuid);
    }

    public UUID toUuid() {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException e) {
            throw new IllegalStateException("Video ID is not a valid UUID: " + value, e);
        }
    }

    public boolean isUuid() {
        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    @Override
    @JsonValue
    public String asString() {
        return value;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        VideoId videoId = (VideoId) o;
        return Objects.equals(value, videoId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "VideoId(" + value + ")";
    }
}

