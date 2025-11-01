package com.youtube.identityauthservice.domain.services;

import com.youtube.identityauthservice.domain.events.*;

/**
 * Domain service interface for publishing events.
 * Abstracts event publishing to maintain clean architecture.
 */
public interface EventPublisher {
    
    /**
     * Publishes a user created event.
     */
    void publishUserCreated(UserCreated event);
    
    /**
     * Publishes a user updated event.
     */
    void publishUserUpdated(UserUpdated event);
    
    /**
     * Publishes a session created event.
     */
    void publishSessionCreated(SessionCreated event);
    
    /**
     * Publishes a session revoked event.
     */
    void publishSessionRevoked(SessionRevoked event);
    
    /**
     * Publishes a refresh token rotated event.
     */
    void publishRefreshTokenRotated(RefreshTokenRotated event);
    
    /**
     * Publishes a refresh token reuse detected event (security event).
     */
    void publishRefreshTokenReuseDetected(RefreshTokenReuseDetected event);
}

