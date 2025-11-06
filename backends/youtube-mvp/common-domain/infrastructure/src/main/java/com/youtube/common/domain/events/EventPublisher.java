package com.youtube.common.domain.events;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.common.domain.core.DomainEvent;
import com.youtube.common.domain.services.correlation.CorrelationContext;
import com.youtube.common.domain.events.outbox.OutboxRepository;
import com.youtube.common.domain.services.tracing.TraceProvider;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Service for publishing domain events using the transactional outbox pattern.
 * Events are stored in the outbox within the same transaction as the business logic.
 */
@Component
public class EventPublisher {
    
    private final OutboxRepository outboxRepository;
    private final TraceProvider traceProvider;
    private final ObjectMapper mapper;
    
    public EventPublisher(OutboxRepository outboxRepository, TraceProvider traceProvider, ObjectMapper mapper) {
        this.outboxRepository = outboxRepository;
        this.traceProvider = traceProvider;
        this.mapper=mapper;
    }
    
    /**
     * Publishes all domain events to the outbox.
     * Events are stored in the database within the current transaction.
     * 
     * @param events the domain events to publish
     */
    public void publishAll(List<DomainEvent> events) {
        if (events == null || events.isEmpty()) {
            return;
        }
        
        // Get correlation context
        String correlationId = CorrelationContext.getCorrelationId().orElse(null);
        String causationId = CorrelationContext.getCausationId().orElse(null);
        String traceparent = traceProvider.getTraceparent();
        
        // Convert to outbox event data
        List<OutboxRepository.OutboxEventData> eventData = events.stream()
            .map(event -> new OutboxRepository.OutboxEventData(
                event.getEventType(),
                extractAggregateType(event),
                extractAggregateId(event),
                serializeEvent(event)
            ))
            .toList();
        
        // Append to outbox (same transaction)
        outboxRepository.append(eventData, correlationId, causationId, traceparent);
    }
    
    private String extractAggregateType(DomainEvent event) {
        // Try to extract from event class name or annotation
        String className = event.getClass().getSimpleName();
        // Remove "Event" suffix if present
        if (className.endsWith("Event")) {
            return className.substring(0, className.length() - 5);
        }
        return className;
    }
    
    private String extractAggregateId(DomainEvent event) {
        // Try to extract aggregate ID from event
        // This is a simplified implementation; subclasses can override
        try {
            var method = event.getClass().getMethod("getAggregateId");
            Object id = method.invoke(event);
            return id != null ? id.toString() : null;
        } catch (Exception e) {
            return null;
        }
    }
    
    private String serializeEvent(DomainEvent event) {
        try {
            return mapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize domain event", e);
        }
    }
}

