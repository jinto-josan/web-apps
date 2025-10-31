package com.youtube.configsecretsservice.domain.valueobject;

import lombok.Value;

/**
 * Value object representing a configuration scope (tenant/environment).
 */
@Value
public class ConfigScope {
    String value;

    private ConfigScope(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Scope cannot be null or empty");
        }
        if (!value.matches("^[a-zA-Z0-9_-]+$")) {
            throw new IllegalArgumentException("Scope must contain only alphanumeric characters, hyphens, and underscores");
        }
        this.value = value;
    }

    public static ConfigScope of(String value) {
        return new ConfigScope(value);
    }

    public String getValue() {
        return value;
    }
}

