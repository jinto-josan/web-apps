package com.youtube.contentidservice.domain.events;

import com.youtube.contentidservice.domain.valueobjects.FingerprintId;
import com.youtube.contentidservice.domain.valueobjects.VideoId;

import java.time.Instant;
import java.util.UUID;

public record FingerprintCreatedEvent(
        UUID eventId,
        FingerprintId fingerprintId,
        VideoId videoId,
        String blobUri,
        Instant occurredAt
) {
    public FingerprintCreatedEvent(FingerprintId fingerprintId, VideoId videoId, String blobUri) {
        this(UUID.randomUUID(), fingerprintId, videoId, blobUri, Instant.now());
    }
}

