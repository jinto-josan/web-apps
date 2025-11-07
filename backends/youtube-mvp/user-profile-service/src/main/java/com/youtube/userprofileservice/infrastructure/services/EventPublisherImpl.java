package com.youtube.userprofileservice.infrastructure.services;

import com.youtube.common.domain.events.EventPublisher;
import com.youtube.userprofileservice.domain.events.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implementation of domain EventPublisher using common-domain EventPublisher.
 * Delegates to common-domain EventPublisher which uses transactional outbox pattern.
 */
@Slf4j
@Component
public class EventPublisherImpl implements com.youtube.userprofileservice.domain.services.EventPublisher {
    
    private final EventPublisher commonDomainEventPublisher;
    
    public EventPublisherImpl(EventPublisher commonDomainEventPublisher) {
        this.commonDomainEventPublisher = commonDomainEventPublisher;
    }
    
    @Override
    public void publishProfileUpdated(ProfileUpdated event) {
        log.debug("Publishing ProfileUpdated event - accountId: {}", event.getAccountId());
        commonDomainEventPublisher.publishAll(List.of(event));
    }
    
    @Override
    public void publishPrivacySettingsChanged(PrivacySettingsChanged event) {
        log.debug("Publishing PrivacySettingsChanged event - accountId: {}", event.getAccountId());
        commonDomainEventPublisher.publishAll(List.of(event));
    }
    
    @Override
    public void publishNotificationPrefsChanged(NotificationPrefsChanged event) {
        log.debug("Publishing NotificationPrefsChanged event - accountId: {}", event.getAccountId());
        commonDomainEventPublisher.publishAll(List.of(event));
    }
    
    @Override
    public void publishAccessibilityPrefsChanged(AccessibilityPrefsChanged event) {
        log.debug("Publishing AccessibilityPrefsChanged event - accountId: {}", event.getAccountId());
        commonDomainEventPublisher.publishAll(List.of(event));
    }
}

