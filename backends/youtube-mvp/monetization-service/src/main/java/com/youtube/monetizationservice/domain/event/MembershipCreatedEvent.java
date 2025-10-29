package com.youtube.monetizationservice.domain.event;

import com.youtube.common.domain.DomainEvent;
import lombok.Getter;

import java.time.Instant;

/**
 * Domain event fired when a new membership is created.
 */
@Getter
public class MembershipCreatedEvent extends DomainEvent {
    private final String membershipId;
    private final String channelId;
    private final String subscriberId;
    private final String tier;
    
    public MembershipCreatedEvent(String membershipId, String channelId, String subscriberId, String tier) {
        super();
        this.membershipId = membershipId;
        this.channelId = channelId;
        this.subscriberId = subscriberId;
        this.tier = tier;
    }
    
    public MembershipCreatedEvent(String eventId, Instant occurredAt, String membershipId, String channelId, String subscriberId, String tier) {
        super(eventId, occurredAt);
        this.membershipId = membershipId;
        this.channelId = channelId;
        this.subscriberId = subscriberId;
        this.tier = tier;
    }
}

