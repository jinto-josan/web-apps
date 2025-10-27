package com.youtube.channelservice.domain.events;

import com.youtube.common.domain.DomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event representing a channel member role change.
 * Published when a member's role in a channel is changed.
 */
@Getter
public final class ChannelMemberRoleChanged extends DomainEvent {
    
    private final String channelId;
    private final String userId;
    private final String oldRole;
    private final String newRole;
    
    public ChannelMemberRoleChanged(String channelId, String userId, String oldRole, String newRole) {
        super();
        this.channelId = Objects.requireNonNull(channelId);
        this.userId = Objects.requireNonNull(userId);
        this.oldRole = oldRole; // Can be null for new members
        this.newRole = Objects.requireNonNull(newRole);
    }
    
    public ChannelMemberRoleChanged(String eventId, Instant occurredAt, String channelId, String userId, String oldRole, String newRole) {
        super(eventId, occurredAt);
        this.channelId = Objects.requireNonNull(channelId);
        this.userId = Objects.requireNonNull(userId);
        this.oldRole = oldRole; // Can be null for new members
        this.newRole = Objects.requireNonNull(newRole);
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        if (!super.equals(obj)) return false;
        ChannelMemberRoleChanged that = (ChannelMemberRoleChanged) obj;
        return Objects.equals(channelId, that.channelId) &&
               Objects.equals(userId, that.userId) &&
               Objects.equals(oldRole, that.oldRole) &&
               Objects.equals(newRole, that.newRole);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(super.hashCode(), channelId, userId, oldRole, newRole);
    }
    
    @Override
    public String toString() {
        return "ChannelMemberRoleChanged{" +
               "eventId='" + getEventId() + '\'' +
               ", occurredAt=" + getOccurredAt() +
               ", channelId='" + channelId + '\'' +
               ", userId='" + userId + '\'' +
               ", oldRole='" + oldRole + '\'' +
               ", newRole='" + newRole + '\'' +
               '}';
    }
}