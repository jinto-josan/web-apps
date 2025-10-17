package com.youtube.common.domain;

/**
 * Interface for managing event schemas.
 * Provides schema registration and retrieval for event versioning.
 */
public interface SchemaRegistry {
    
    /**
     * Gets the schema for a specific event type and version.
     * 
     * @param eventType the type of the event
     * @param version the version of the event
     * @return the schema as a JSON string
     */
    String getSchema(String eventType, int version);
    
    /**
     * Registers a schema for a specific event type and version.
     * 
     * @param eventType the type of the event
     * @param version the version of the event
     * @param schema the schema as a JSON string
     */
    void register(String eventType, int version, String schema);
}
