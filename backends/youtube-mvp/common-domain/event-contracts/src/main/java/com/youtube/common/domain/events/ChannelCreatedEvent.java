package com.youtube.common.domain.events;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.youtube.common.domain.DomainEvent;
import com.youtube.common.domain.shared.valueobjects.ChannelId;
import com.youtube.common.domain.shared.valueobjects.UserId;

import java.time.Instant;

/**
 * Domain event published when a new channel is created.
 * 
 * <p>Published by: channel-service
 * <p>Consumed by:
 * <ul>
 *   <li>user-profile-service - Link channel to user profile</li>
 *   <li>monetization-service - Initialize monetization settings</li>
 *   <li>notifications-service - Send channel creation notification</li>
 * </ul>
 */
public final class ChannelCreatedEvent extends DomainEvent {
    
    private final ChannelId channelId;
    private final UserId ownerId;
    private final String channelName;
    private final String description;

    @JsonCreator
    public ChannelCreatedEvent(
            @JsonProperty("eventId") String eventId,
            @JsonProperty("occurredAt") Instant occurredAt,
            @JsonProperty("channelId") ChannelId channelId,
            @JsonProperty("ownerId") UserId ownerId,
            @JsonProperty("channelName") String channelName,
            @JsonProperty("description") String description) {
        super(eventId, occurredAt);
        this.channelId = channelId;
        this.ownerId = ownerId;
        this.channelName = channelName;
        this.description = description;
    }

    public ChannelCreatedEvent(ChannelId channelId, UserId ownerId, String channelName, String description) {
        this.channelId = channelId;
        this.ownerId = ownerId;
        this.channelName = channelName;
        this.description = description;
    }

    public ChannelId getChannelId() {
        return channelId;
    }

    public UserId getOwnerId() {
        return ownerId;
    }

    public String getChannelName() {
        return channelName;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String getEventType() {
        return "channel.created";
    }
}

