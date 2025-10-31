package com.youtube.contentidservice.domain.valueobjects;

import lombok.Value;
import java.util.UUID;

@Value
public class ClaimId {
    UUID value;

    public static ClaimId of(UUID value) {
        if (value == null) {
            throw new IllegalArgumentException("ClaimId cannot be null");
        }
        return new ClaimId(value);
    }

    public static ClaimId of(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("ClaimId cannot be null or blank");
        }
        return new ClaimId(UUID.fromString(value));
    }
}

