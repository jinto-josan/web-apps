package com.youtube.contentidservice.domain.valueobjects;

import lombok.Value;
import java.util.UUID;

@Value
public class FingerprintId {
    UUID value;

    public static FingerprintId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("FingerprintId cannot be null");
        }
        return new FingerprintId(value);
    }

    public static FingerprintId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("FingerprintId cannot be null or blank");
        }
        return new FingerprintId(UUID.fromString(value));
    }
}

