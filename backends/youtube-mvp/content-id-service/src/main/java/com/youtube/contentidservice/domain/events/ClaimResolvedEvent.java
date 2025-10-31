package com.youtube.contentidservice.domain.events;

import com.youtube.contentidservice.domain.valueobjects.ClaimId;
import com.youtube.contentidservice.domain.valueobjects.DisputeStatus;

import java.time.Instant;
import java.util.UUID;

public record ClaimResolvedEvent(
        UUID eventId,
        ClaimId claimId,
        DisputeStatus disputeStatus,
        String resolution,
        Instant occurredAt
) {
    public ClaimResolvedEvent(ClaimId claimId, DisputeStatus disputeStatus, String resolution) {
        this(UUID.randomUUID(), claimId, disputeStatus, resolution, Instant.now());
    }
}

