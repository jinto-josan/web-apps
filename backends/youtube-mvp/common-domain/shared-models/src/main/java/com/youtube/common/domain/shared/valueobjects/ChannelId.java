package com.youtube.common.domain.shared.valueobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.youtube.common.domain.core.Identifier;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

/**
 * Value object representing a channel identifier.
 * Immutable and type-safe.
 * 
 * <p>Used across multiple services:
 * <ul>
 *   <li>channel-service - Channel management</li>
 *   <li>video-catalog-service - Video-channel relationships</li>
 *   <li>monetization-service - Channel monetization</li>
 *   <li>studio-analytics-service - Channel analytics</li>
 * </ul>
 */
public final class ChannelId implements Identifier {
    
    @NotBlank
    private final String value;

    @JsonCreator
    private ChannelId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Channel ID cannot be null or blank");
        }
        this.value = value.trim();
    }

    public static ChannelId from(String value) {
        return new ChannelId(value);
    }

    public static ChannelId of(String value) {
        return from(value);
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
        ChannelId channelId = (ChannelId) o;
        return Objects.equals(value, channelId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "ChannelId(" + value + ")";
    }
}

