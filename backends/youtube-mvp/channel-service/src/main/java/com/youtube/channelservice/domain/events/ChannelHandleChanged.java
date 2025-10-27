package com.youtube.channelservice.domain.events;

import com.youtube.common.domain.DomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event representing a channel handle change.
 * Published when a channel's handle is successfully changed.
 */
@Getter
public final class ChannelHandleChanged extends DomainEvent {
    
    private final String channelId;
    private final String oldHandleLower;
    private final String newHandleLower;
    
    public ChannelHandleChanged(String channelId, String oldHandleLower, String newHandleLower) {
        super();
        this.channelId = Objects.requireNonNull(channelId);
        this.oldHandleLower = Objects.requireNonNull(oldHandleLower);
        this.newHandleLower = Objects.requireNonNull(newHandleLower);
    }
    
    public ChannelHandleChanged(String eventId, Instant occurredAt, String channelId, String oldHandleLower, String newHandleLower) {
        super(eventId, occurredAt);
        this.channelId = Objects.requireNonNull(channelId);
        this.oldHandleLower = Objects.requireNonNull(oldHandleLower);
        this.newHandleLower = Objects.requireNonNull(newHandleLower);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        ChannelHandleChanged that = (ChannelHandleChanged) obj;
        return Objects.equals(channelId, that.channelId) &&
               Objects.equals(oldHandleLower, that.oldHandleLower) &&
               Objects.equals(newHandleLower, that.newHandleLower);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), channelId, oldHandleLower, newHandleLower);
    }
    
    @Override
    public String toString() {
        return "ChannelHandleChanged{" +
               "eventId='" + getEventId() + '\'' +
               ", occurredAt=" + getOccurredAt() +
               ", channelId='" + channelId + '\'' +
               ", oldHandleLower='" + oldHandleLower + '\'' +
               ", newHandleLower='" + newHandleLower + '\'' +
               '}';
    }
}