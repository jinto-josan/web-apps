package com.youtube.analyticstelemetryservice.domain.repositories;

import com.youtube.analyticstelemetryservice.domain.entities.TelemetryEvent;

import java.util.List;
import java.util.Optional;

/**
 * Repository port for telemetry event persistence.
 * Part of the domain layer - implemented by infrastructure adapters.
 */
public interface TelemetryEventRepository {
    
    /**
     * Save a single telemetry event.
     * @param event the event to save
     * @return the saved event
     */
    TelemetryEvent save(TelemetryEvent event);
    
    /**
     * Save a batch of telemetry events.
     * @param events the events to save
     * @return the saved events
     */
    List<TelemetryEvent> saveAll(List<TelemetryEvent> events);
    
    /**
     * Find an event by ID.
     * @param eventId the event ID
     * @return the event if found
     */
    Optional<TelemetryEvent> findById(String eventId);
    
    /**
     * Check if an event with the given ID exists (for idempotency).
     * @param eventId the event ID
     * @return true if exists
     */
    boolean existsById(String eventId);
}

