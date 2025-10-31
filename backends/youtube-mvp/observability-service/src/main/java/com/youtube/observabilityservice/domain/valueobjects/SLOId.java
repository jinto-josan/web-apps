package com.youtube.observabilityservice.domain.valueobjects;

import lombok.Value;

import java.util.UUID;

@Value
public class SLOId {
    UUID value;

    public static SLOId random() {
        return new SLOId(UUID.randomUUID());
    }

    public static SLOId from(String uuid) {
        return new SLOId(UUID.fromString(uuid));
    }
}

