package com.youtube.identityauthservice.domain.valueobjects;

import com.youtube.common.domain.core.Identifier;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * Type-safe identifier for Session entities.
 */
public record SessionId(@NotBlank @Pattern(regexp = "^[0-9A-HJKMNP-TV-Z]{26}$") String value) implements Identifier<String> {
    
    public static SessionId from(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Session ID cannot be null or blank");
        }
        return new SessionId(value);
    }
    
    @Override
    public String getValue() {
        return value;
    }
    
    @Override
    public String asString() {
        return value;
    }
}

