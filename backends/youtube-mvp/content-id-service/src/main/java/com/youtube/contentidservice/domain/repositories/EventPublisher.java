package com.youtube.contentidservice.domain.repositories;

/**
 * Port for publishing domain events (Outbox pattern will be used in infrastructure)
 */
public interface EventPublisher {
    void publish(Object domainEvent);
}

