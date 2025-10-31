package com.youtube.analyticstelemetryservice.domain;

import com.youtube.analyticstelemetryservice.domain.entities.TelemetryEvent;
import com.youtube.analyticstelemetryservice.domain.valueobjects.*;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TelemetryEventTest {
    
    @Test
    void shouldCreateValidEvent() {
        TelemetryEvent event = TelemetryEvent.builder()
            .eventId(EventId.generate())
            .eventType(EventType.VIDEO_VIEW)
            .eventSource(EventSource.WEB_CLIENT)
            .schema(EventSchema.V1)
            .timestamp(Instant.now())
            .userId("user-123")
            .sessionId("session-456")
            .correlationId("corr-789")
            .build();
        
        assertThat(event).isNotNull();
        assertThat(event.getEventId()).isNotNull();
        assertThat(event.getEventType().getValue()).isEqualTo("video.view");
    }
    
    @Test
    void shouldValidateEvent() {
        TelemetryEvent event = TelemetryEvent.builder()
            .eventId(EventId.generate())
            .eventType(EventType.VIDEO_VIEW)
            .eventSource(EventSource.WEB_CLIENT)
            .schema(EventSchema.V1)
            .timestamp(Instant.now())
            .build();
        
        // Should not throw
        event.validate();
    }
    
    @Test
    void shouldThrowOnMissingEventId() {
        TelemetryEvent event = TelemetryEvent.builder()
            .eventType(EventType.VIDEO_VIEW)
            .eventSource(EventSource.WEB_CLIENT)
            .schema(EventSchema.V1)
            .timestamp(Instant.now())
            .build();
        
        assertThatThrownBy(event::validate)
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("Event ID is required");
    }
    
    @Test
    void shouldGenerateEventIdIfNotProvided() {
        EventId eventId = new EventId(null);
        assertThat(eventId.getValue()).isNotNull();
        assertThat(eventId.getValue()).isNotEmpty();
    }
}

