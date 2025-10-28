package com.youtube.mvp.videocatalog.infrastructure.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.mvp.videocatalog.domain.event.VideoPublishedEvent;
import com.youtube.mvp.videocatalog.infrastructure.outbox.OutboxEvent;
import com.youtube.mvp.videocatalog.infrastructure.outbox.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

/**
 * Event publisher using Outbox pattern for reliable messaging.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class VideoEventPublisher {
    
    private final ObjectMapper objectMapper;
    private final OutboxRepository outboxRepository;
    
    /**
     * Publishes video published event via Outbox pattern.
     */
    public void publishVideoPublishedEvent(VideoPublishedEvent event) {
        log.info("Publishing video published event: {}", event.getVideoId());
        
        try {
            String payload = objectMapper.writeValueAsString(event);
            
            OutboxEvent outboxEvent = OutboxEvent.builder()
                    .eventId(UUID.randomUUID().toString())
                    .aggregateType("Video")
                    .aggregateId(event.getVideoId())
                    .eventType("VideoPublished")
                    .payload(payload)
                    .occurredAt(Instant.now())
                    .status("PENDING")
                    .build();
            
            outboxRepository.save(outboxEvent);
            log.debug("Event added to outbox: {}", outboxEvent.getEventId());
            
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event", e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}

