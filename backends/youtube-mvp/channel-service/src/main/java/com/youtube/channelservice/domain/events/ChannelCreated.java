package com.youtube.channelservice.domain.events;

import com.youtube.common.domain.DomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event representing a channel creation.
 * Published when a new channel is successfully created.
 */
@Getter
public final class ChannelCreated extends DomainEvent {
    
    private final String channelId;
    private final String ownerUserId;
    private final String handleLower;
    
    public ChannelCreated(String channelId, String ownerUserId, String handleLower) {
        super();
        this.channelId = Objects.requireNonNull(channelId);
        this.ownerUserId = Objects.requireNonNull(ownerUserId);
        this.handleLower = Objects.requireNonNull(handleLower);
    }
    
    public ChannelCreated(String eventId, Instant occurredAt, String channelId, String ownerUserId, String handleLower) {
        super(eventId, occurredAt);
        this.channelId = Objects.requireNonNull(channelId);
        this.ownerUserId = Objects.requireNonNull(ownerUserId);
        this.handleLower = Objects.requireNonNull(handleLower);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        ChannelCreated that = (ChannelCreated) obj;
        return Objects.equals(channelId, that.channelId) &&
               Objects.equals(ownerUserId, that.ownerUserId) &&
               Objects.equals(handleLower, that.handleLower);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), channelId, ownerUserId, handleLower);
    }
    
    @Override
    public String toString() {
        return "ChannelCreated{" +
               "eventId='" + getEventId() + '\'' +
               ", occurredAt=" + getOccurredAt() +
               ", channelId='" + channelId + '\'' +
               ", ownerUserId='" + ownerUserId + '\'' +
               ", handleLower='" + handleLower + '\'' +
               '}';
    }
}