package com.youtube.userprofileservice.domain.events;

import com.youtube.common.domain.DomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event published when accessibility preferences are changed.
 */
@Getter
public final class AccessibilityPrefsChanged extends DomainEvent {
    
    private final String accountId;
    private final String updatedBy;
    private final String preferenceChanged; // e.g., "captionsAlwaysOn", "autoplayDefault"
    private final String newValue;
    
    public AccessibilityPrefsChanged(String accountId, String updatedBy, String preferenceChanged, String newValue) {
        super();
        this.accountId = Objects.requireNonNull(accountId);
        this.updatedBy = updatedBy;
        this.preferenceChanged = preferenceChanged;
        this.newValue = newValue;
    }
    
    public AccessibilityPrefsChanged(String eventId, Instant occurredAt, String accountId, String updatedBy, String preferenceChanged, String newValue) {
        super(eventId, occurredAt);
        this.accountId = Objects.requireNonNull(accountId);
        this.updatedBy = updatedBy;
        this.preferenceChanged = preferenceChanged;
        this.newValue = newValue;
    }
}

