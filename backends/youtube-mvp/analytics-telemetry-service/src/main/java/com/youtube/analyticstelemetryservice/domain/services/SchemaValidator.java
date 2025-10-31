package com.youtube.analyticstelemetryservice.domain.services;

import com.youtube.analyticstelemetryservice.domain.entities.TelemetryEvent;

/**
 * Domain service for schema validation.
 * Validates events against JSON schemas.
 */
public interface SchemaValidator {
    
    /**
     * Validate an event against its schema.
     * @param event the event to validate
     * @throws IllegalArgumentException if validation fails
     */
    void validate(TelemetryEvent event);
    
    /**
     * Check if a schema version is supported.
     * @param schemaVersion the schema version
     * @return true if supported
     */
    boolean isSchemaSupported(String schemaVersion);
}

