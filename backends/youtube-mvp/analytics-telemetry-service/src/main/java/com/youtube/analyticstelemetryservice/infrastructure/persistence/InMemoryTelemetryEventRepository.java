package com.youtube.analyticstelemetryservice.infrastructure.persistence;

import com.youtube.analyticstelemetryservice.domain.entities.TelemetryEvent;
import com.youtube.analyticstelemetryservice.domain.repositories.TelemetryEventRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * In-memory repository implementation for telemetry events.
 * Used for local development and testing.
 * For production, consider using Cosmos DB or PostgreSQL.
 */
@Slf4j
@Repository
@ConditionalOnProperty(name = "telemetry.repository.type", havingValue = "memory", matchIfMissing = true)
public class InMemoryTelemetryEventRepository implements TelemetryEventRepository {
    
    private final Map<String, TelemetryEvent> storage = new ConcurrentHashMap<>();
    
    @Override
    public TelemetryEvent save(TelemetryEvent event) {
        storage.put(event.getEventId().getValue(), event);
        return event;
    }
    
    @Override
    public List<TelemetryEvent> saveAll(List<TelemetryEvent> events) {
        events.forEach(event -> storage.put(event.getEventId().getValue(), event));
        return new ArrayList<>(events);
    }
    
    @Override
    public Optional<TelemetryEvent> findById(String eventId) {
        return Optional.ofNullable(storage.get(eventId));
    }
    
    @Override
    public boolean existsById(String eventId) {
        return storage.containsKey(eventId);
    }
}

