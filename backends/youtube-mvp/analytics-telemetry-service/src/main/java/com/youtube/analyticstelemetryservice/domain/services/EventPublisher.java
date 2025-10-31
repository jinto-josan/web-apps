package com.youtube.analyticstelemetryservice.domain.services;

import com.youtube.analyticstelemetryservice.domain.entities.TelemetryEvent;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * Domain service port for publishing events to Event Hubs.
 * Part of the domain layer - implemented by infrastructure adapters.
 */
public interface EventPublisher {
    
    /**
     * Publish a single event to Event Hubs.
     * @param event the event to publish
     * @return CompletableFuture that completes when the event is published
     */
    CompletableFuture<Void> publish(TelemetryEvent event);
    
    /**
     * Publish a batch of events to Event Hubs.
     * Implementations should batch events for efficiency.
     * @param events the events to publish
     * @return CompletableFuture that completes when all events are published
     */
    CompletableFuture<Void> publishBatch(List<TelemetryEvent> events);
    
    /**
     * Check if the publisher is healthy and can accept events.
     * @return true if healthy
     */
    boolean isHealthy();
}

