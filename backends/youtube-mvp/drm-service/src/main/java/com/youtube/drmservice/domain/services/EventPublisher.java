package com.youtube.drmservice.domain.services;

/**
 * Port for publishing domain events
 */
public interface EventPublisher {
    void publishPolicyCreated(Object event);
    void publishPolicyUpdated(Object event);
    void publishKeyRotationTriggered(Object event);
}

