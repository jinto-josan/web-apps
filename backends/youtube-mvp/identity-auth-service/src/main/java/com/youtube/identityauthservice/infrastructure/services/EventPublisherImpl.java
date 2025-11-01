package com.youtube.identityauthservice.infrastructure.services;

import com.youtube.identityauthservice.domain.events.*;
import com.youtube.identityauthservice.domain.services.EventPublisher;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Implementation of domain EventPublisher using common-domain EventPublisher.
 * Delegates to common-domain EventPublisher which uses transactional outbox pattern.
 */
@Component
public class EventPublisherImpl implements EventPublisher {

    private final com.youtube.common.domain.events.EventPublisher commonDomainEventPublisher;

    public EventPublisherImpl(com.youtube.common.domain.events.EventPublisher commonDomainEventPublisher) {
        this.commonDomainEventPublisher = commonDomainEventPublisher;
    }

    @Override
    public void publishUserCreated(UserCreated event) {
        commonDomainEventPublisher.publishAll(List.of(event));
    }

    @Override
    public void publishUserUpdated(UserUpdated event) {
        commonDomainEventPublisher.publishAll(List.of(event));
    }

    @Override
    public void publishSessionCreated(SessionCreated event) {
        commonDomainEventPublisher.publishAll(List.of(event));
    }

    @Override
    public void publishSessionRevoked(SessionRevoked event) {
        commonDomainEventPublisher.publishAll(List.of(event));
    }

    @Override
    public void publishRefreshTokenRotated(RefreshTokenRotated event) {
        commonDomainEventPublisher.publishAll(List.of(event));
    }

    @Override
    public void publishRefreshTokenReuseDetected(RefreshTokenReuseDetected event) {
        commonDomainEventPublisher.publishAll(List.of(event));
    }
}

