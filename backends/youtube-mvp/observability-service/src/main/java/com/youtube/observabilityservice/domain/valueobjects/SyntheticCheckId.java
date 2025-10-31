package com.youtube.observabilityservice.domain.valueobjects;

import lombok.Value;

import java.util.UUID;

@Value
public class SyntheticCheckId {
    UUID value;

    public static SyntheticCheckId random() {
        return new SyntheticCheckId(UUID.randomUUID());
    }

    public static SyntheticCheckId from(String uuid) {
        return new SyntheticCheckId(UUID.fromString(uuid));
    }
}

