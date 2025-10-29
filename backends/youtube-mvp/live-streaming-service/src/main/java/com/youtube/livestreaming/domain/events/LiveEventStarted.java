package com.youtube.livestreaming.domain.events;

import java.time.Instant;
import java.util.UUID;

public record LiveEventStarted(
    UUID eventId,
    String liveEventId,
    String userId,
    String channelId,
    Instant occurredAt
) {
    public LiveEventStarted {
        if (eventId == null) eventId = UUID.randomUUID();
        if (occurredAt == null) occurredAt = Instant.now();
    }
    
    public LiveEventStarted(String liveEventId, String userId, String channelId) {
        this(UUID.randomUUID(), liveEventId, userId, channelId, Instant.now());
    }
}

