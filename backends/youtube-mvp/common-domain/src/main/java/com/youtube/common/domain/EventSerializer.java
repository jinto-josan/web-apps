package com.youtube.common.domain;

/**
 * Interface for serializing and deserializing domain events.
 * Handles the conversion between domain events and their JSON representation.
 */
public interface EventSerializer {
    
    /**
     * Serializes a domain event with its metadata to JSON.
     * 
     * @param event the domain event to serialize
     * @param metadata the event metadata
     * @return JSON string representation
     */
    String serialize(DomainEvent event, EventMetadata metadata);
    
    /**
     * Deserializes a JSON payload back to a domain event.
     * 
     * @param eventType the type of the event
     * @param version the version of the event
     * @param payloadJson the JSON payload
     * @return the deserialized domain event
     */
    DomainEvent deserialize(String eventType, int version, String payloadJson);
}
