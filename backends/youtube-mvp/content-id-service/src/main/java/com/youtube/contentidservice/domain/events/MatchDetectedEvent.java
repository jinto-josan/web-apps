package com.youtube.contentidservice.domain.events;

import com.youtube.contentidservice.domain.valueobjects.FingerprintId;
import com.youtube.contentidservice.domain.valueobjects.MatchScore;
import com.youtube.contentidservice.domain.valueobjects.VideoId;

import java.time.Instant;
import java.util.UUID;

public record MatchDetectedEvent(
        UUID eventId,
        UUID matchId,
        FingerprintId sourceFingerprintId,
        FingerprintId matchedFingerprintId,
        VideoId sourceVideoId,
        VideoId matchedVideoId,
        MatchScore score,
        Instant occurredAt
) {
    public MatchDetectedEvent(
            UUID matchId,
            FingerprintId sourceFingerprintId,
            FingerprintId matchedFingerprintId,
            VideoId sourceVideoId,
            VideoId matchedVideoId,
            MatchScore score) {
        this(UUID.randomUUID(), matchId, sourceFingerprintId, matchedFingerprintId, 
                sourceVideoId, matchedVideoId, score, Instant.now());
    }
}

