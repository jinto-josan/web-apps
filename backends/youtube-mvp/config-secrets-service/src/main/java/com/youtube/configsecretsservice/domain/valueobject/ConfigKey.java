package com.youtube.configsecretsservice.domain.valueobject;

import lombok.Value;

/**
 * Value object representing a configuration key.
 */
@Value
public class ConfigKey {
    String value;

    private ConfigKey(String value) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        if (!value.matches("^[a-zA-Z0-9._-]+$")) {
            throw new IllegalArgumentException("Key must contain only alphanumeric characters, dots, hyphens, and underscores");
        }
        this.value = value;
    }

    public static ConfigKey of(String value) {
        return new ConfigKey(value);
    }

    public String getValue() {
        return value;
    }
}

