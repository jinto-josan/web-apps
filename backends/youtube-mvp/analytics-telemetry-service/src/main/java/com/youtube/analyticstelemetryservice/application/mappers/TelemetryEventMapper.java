package com.youtube.analyticstelemetryservice.application.mappers;

import com.youtube.analyticstelemetryservice.application.dto.TelemetryEventRequest;
import com.youtube.analyticstelemetryservice.application.dto.TelemetryEventResponse;
import com.youtube.analyticstelemetryservice.domain.entities.TelemetryEvent;
import com.youtube.analyticstelemetryservice.domain.valueobjects.EventId;
import com.youtube.analyticstelemetryservice.domain.valueobjects.EventSchema;
import com.youtube.analyticstelemetryservice.domain.valueobjects.EventSource;
import com.youtube.analyticstelemetryservice.domain.valueobjects.EventType;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.NullValuePropertyMappingStrategy;

/**
 * MapStruct mapper for telemetry events.
 */
@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface TelemetryEventMapper {
    
    @Mapping(target = "eventId", expression = "java(mapEventId(request.getEventId()))")
    @Mapping(target = "eventType", expression = "java(EventType.of(request.getEventType()))")
    @Mapping(target = "eventSource", expression = "java(mapEventSource(request.getEventSource(), request.getIsClient()))")
    @Mapping(target = "schema", expression = "java(mapSchema(request.getSchemaVersion(), request.getSchemaName()))")
    @Mapping(target = "timestamp", source = "timestamp")
    @Mapping(target = "userId", source = "userId")
    @Mapping(target = "sessionId", source = "sessionId")
    @Mapping(target = "properties", source = "properties")
    @Mapping(target = "correlationId", source = "correlationId")
    TelemetryEvent toDomain(TelemetryEventRequest request);
    
    @Mapping(target = "eventId", source = "eventId.value")
    @Mapping(target = "status", source = "status")
    @Mapping(target = "message", source = "message")
    @Mapping(target = "processedAt", source = "processedAt")
    TelemetryEventResponse toResponse(String eventId, String status, String message, java.time.Instant processedAt);
    
    default EventId mapEventId(String eventId) {
        return eventId != null ? EventId.of(eventId) : EventId.generate();
    }
    
    default EventSource mapEventSource(String source, Boolean isClient) {
        if (source == null) {
            throw new IllegalArgumentException("Event source cannot be null");
        }
        boolean client = isClient != null ? isClient : source.toLowerCase().contains("client") || 
                                                      source.equalsIgnoreCase("web") || 
                                                      source.equalsIgnoreCase("mobile");
        return client ? EventSource.client(source) : EventSource.server(source);
    }
    
    default EventSchema mapSchema(String version, String name) {
        String schemaVersion = version != null ? version : "1.0";
        String schemaName = name != null ? name : "telemetry-event-v1";
        return EventSchema.of(schemaVersion, schemaName);
    }
}

