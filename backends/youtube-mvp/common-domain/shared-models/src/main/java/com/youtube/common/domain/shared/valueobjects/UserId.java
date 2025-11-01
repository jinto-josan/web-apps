package com.youtube.common.domain.shared.valueobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import com.youtube.common.domain.core.Identifier;
import jakarta.validation.constraints.NotBlank;

import java.util.Objects;

/**
 * Value object representing a user identifier.
 * Immutable and type-safe.
 * 
 * <p>Used across multiple services:
 * <ul>
 *   <li>user-profile-service - User profiles</li>
 *   <li>channel-service - Channel ownership</li>
 *   <li>engagement-service - User engagement tracking</li>
 *   <li>recommendations-service - User-based recommendations</li>
 *   <li>history-service - Watch history</li>
 * </ul>
 */
public final class UserId implements Identifier {
    
    @NotBlank
    private final String value;

    @JsonCreator
    private UserId(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("User ID cannot be null or blank");
        }
        this.value = value.trim();
    }

    public static UserId from(String value) {
        return new UserId(value);
    }

    public static UserId of(String value) {
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
        UserId userId = (UserId) o;
        return Objects.equals(value, userId.value);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value);
    }

    @Override
    public String toString() {
        return "UserId(" + value + ")";
    }
}

