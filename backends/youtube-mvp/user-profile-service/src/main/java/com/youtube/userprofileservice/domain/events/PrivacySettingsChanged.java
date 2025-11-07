package com.youtube.userprofileservice.domain.events;

import com.youtube.common.domain.core.DomainEvent;
import lombok.Getter;

import java.time.Instant;
import java.util.Objects;

/**
 * Domain event published when privacy settings are changed.
 */
@Getter
public final class PrivacySettingsChanged extends DomainEvent {
    
    private final String accountId;
    private final String updatedBy;
    private final String settingChanged; // e.g., "subscriptionsPrivate", "savedPlaylistsPrivate"
    private final Boolean newValue;
    
    public PrivacySettingsChanged(String accountId, String updatedBy, String settingChanged, Boolean newValue) {
        super();
        this.accountId = Objects.requireNonNull(accountId);
        this.updatedBy = updatedBy;
        this.settingChanged = settingChanged;
        this.newValue = newValue;
    }
    
    public PrivacySettingsChanged(String eventId, Instant occurredAt, String accountId, String updatedBy, String settingChanged, Boolean newValue) {
        super(eventId, occurredAt);
        this.accountId = Objects.requireNonNull(accountId);
        this.updatedBy = updatedBy;
        this.settingChanged = settingChanged;
        this.newValue = newValue;
    }
    
    @Override
    public String getEventType() {
        return "privacy.settings.changed";
    }
}

