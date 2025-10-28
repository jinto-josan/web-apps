package com.youtube.userprofileservice.domain.events;

import com.youtube.common.domain.DomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event published when a user's profile is updated.
 */
@Getter
public final class ProfileUpdated extends DomainEvent {
    
    private final String accountId;
    private final String updatedBy;
    private final String fieldsChanged; // JSON array of changed field names
    
    public ProfileUpdated(String accountId, String updatedBy, String fieldsChanged) {
        super();
        this.accountId = Objects.requireNonNull(accountId);
        this.updatedBy = updatedBy;
        this.fieldsChanged = fieldsChanged;
    }
    
    public ProfileUpdated(String eventId, Instant occurredAt, String accountId, String updatedBy, String fieldsChanged) {
        super(eventId, occurredAt);
        this.accountId = Objects.requireNonNull(accountId);
        this.updatedBy = updatedBy;
        this.fieldsChanged = fieldsChanged;
    }
}

