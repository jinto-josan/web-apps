package com.youtube.userprofileservice.domain.events;

import com.youtube.common.domain.core.DomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event published when notification preferences are changed.
 */
@Getter
public final class NotificationPrefsChanged extends DomainEvent {
    
    private final String accountId;
    private final String updatedBy;
    private final String preferenceChanged; // e.g., "emailOptIn", "pushOptIn"
    private final Boolean newValue;
    
    public NotificationPrefsChanged(String accountId, String updatedBy, String preferenceChanged, Boolean newValue) {
        super();
        this.accountId = Objects.requireNonNull(accountId);
        this.updatedBy = updatedBy;
        this.preferenceChanged = preferenceChanged;
        this.newValue = newValue;
    }
    
    public NotificationPrefsChanged(String eventId, Instant occurredAt, String accountId, String updatedBy, String preferenceChanged, Boolean newValue) {
        super(eventId, occurredAt);
        this.accountId = Objects.requireNonNull(accountId);
        this.updatedBy = updatedBy;
        this.preferenceChanged = preferenceChanged;
        this.newValue = newValue;
    }
}

