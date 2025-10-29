package com.youtube.livestreaming.domain.ports;

/**
 * Port for publishing domain events
 */
public interface EventPublisher {
    void publish(Object event);
    
    void publishAll(Iterable<Object> events);
}

