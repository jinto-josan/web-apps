package com.youtube.common.domain.events;

import com.youtube.common.domain.core.DomainEvent;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Service for routing domain events to appropriate handlers.
 * Maintains a registry of event type to handler mappings.
 */
public class EventRouter {
    
    private final Map<String, EventHandler<DomainEvent>> handlers = new ConcurrentHashMap<>();
    
    /**
     * Registers an event handler for a specific event type.
     * 
     * @param eventType the event type
     * @param handler the handler
     */
    public void registerHandler(String eventType, EventHandler<DomainEvent> handler) {
        handlers.put(eventType, handler);
    }
    
    /**
     * Resolves the handler for a given event type.
     * 
     * @param eventType the event type
     * @return the handler, or null if not found
     */
    public EventHandler<DomainEvent> resolveHandler(String eventType) {
        return handlers.get(eventType);
    }
    
    /**
     * Resolves the handler for a given domain event.
     * 
     * @param event the domain event
     * @return the handler, or null if not found
     */
    public EventHandler<DomainEvent> resolveHandler(DomainEvent event) {
        return resolveHandler(event.getEventType());
    }
    
    /**
     * Interface for event handlers.
     * 
     * @param <T> the event type
     */
    public interface EventHandler<T extends DomainEvent> {
        /**
         * Handles the domain event.
         * 
         * @param event the domain event
         * @param correlationId the correlation ID
         */
        void handle(T event, String correlationId);
    }
}

