package com.youtube.contentidservice.domain.events;

import com.youtube.contentidservice.domain.valueobjects.ClaimId;
import com.youtube.contentidservice.domain.valueobjects.VideoId;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ClaimCreatedEvent(
        UUID eventId,
        ClaimId claimId,
        VideoId claimedVideoId,
        UUID ownerId,
        List<UUID> matchIds,
        Instant occurredAt
) {
    public ClaimCreatedEvent(ClaimId claimId, VideoId claimedVideoId, UUID ownerId, List<UUID> matchIds) {
        this(UUID.randomUUID(), claimId, claimedVideoId, ownerId, matchIds, Instant.now());
    }
}

