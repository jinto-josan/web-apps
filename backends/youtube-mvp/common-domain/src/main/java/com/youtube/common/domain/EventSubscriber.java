package com.youtube.common.domain;

import java.util.List;

/**
 * Interface for subscribing to domain events.
 * Implementations handle event processing logic.
 */
public interface EventSubscriber {
    
    /**
     * Handles an incoming event message.
     * 
     * @param envelope the event envelope containing the event and metadata
     */
    void onMessage(EventEnvelope envelope);
    
    /**
     * Returns the list of event types this subscriber handles.
     * 
     * @return list of event type names
     */
    List<String> eventTypes();
}
