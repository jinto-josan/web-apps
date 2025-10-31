package com.youtube.analyticstelemetryservice.infrastructure.adapters.schema;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.erosb.everit.json.schema.Schema;
import com.github.erosb.everit.json.schema.loader.SchemaLoader;
import com.youtube.analyticstelemetryservice.domain.entities.TelemetryEvent;
import com.youtube.analyticstelemetryservice.domain.services.SchemaValidator;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Infrastructure adapter for schema validation using JSON Schema.
 * Loads schemas from resources and validates events.
 */
@Slf4j
@Service
public class JsonSchemaValidator implements SchemaValidator {
    
    private final ObjectMapper objectMapper;
    private final ResourceLoader resourceLoader;
    private final Map<String, Schema> schemaCache = new ConcurrentHashMap<>();
    
    @Value("${telemetry.schema.default-version:1.0}")
    private String defaultSchemaVersion;
    
    public JsonSchemaValidator(ObjectMapper objectMapper, ResourceLoader resourceLoader) {
        this.objectMapper = objectMapper;
        this.resourceLoader = resourceLoader;
    }
    
    @Override
    public void validate(TelemetryEvent event) {
        String schemaVersion = event.getSchema().getVersion();
        if (!isSchemaSupported(schemaVersion)) {
            throw new IllegalArgumentException("Schema version not supported: " + schemaVersion);
        }
        
        Schema schema = getSchema(schemaVersion);
        if (schema == null) {
            log.warn("Schema not found for version: {}. Skipping validation.", schemaVersion);
            return;
        }
        
        try {
            JSONObject eventJson = convertToJsonObject(event);
            schema.validate(eventJson);
        } catch (com.github.erosb.everit.json.schema.ValidationException e) {
            log.error("Schema validation failed for event: {}", event.getEventId().getValue(), e);
            throw new IllegalArgumentException("Schema validation failed: " + e.getMessage(), e);
        }
    }
    
    @Override
    public boolean isSchemaSupported(String schemaVersion) {
        // For now, only support version 1.0
        return "1.0".equals(schemaVersion) || defaultSchemaVersion.equals(schemaVersion);
    }
    
    private Schema getSchema(String schemaVersion) {
        return schemaCache.computeIfAbsent(schemaVersion, version -> {
            try {
                String schemaPath = "/schemas/telemetry-event-" + version + ".json";
                InputStream schemaStream = resourceLoader.getResource("classpath:" + schemaPath).getInputStream();
                
                if (schemaStream == null) {
                    log.warn("Schema file not found: {}", schemaPath);
                    return null;
                }
                
                JSONObject schemaJson = new JSONObject(new String(schemaStream.readAllBytes()));
                return SchemaLoader.load(schemaJson);
            } catch (Exception e) {
                log.error("Failed to load schema for version: {}", version, e);
                return null;
            }
        });
    }
    
    private JSONObject convertToJsonObject(TelemetryEvent event) {
        try {
            String json = objectMapper.writeValueAsString(event);
            return new JSONObject(json);
        } catch (Exception e) {
            throw new RuntimeException("Failed to convert event to JSON", e);
        }
    }
}

