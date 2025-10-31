package com.youtube.contentidservice.infrastructure.messaging;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.contentidservice.domain.repositories.EventPublisher;
import com.youtube.contentidservice.infrastructure.persistence.JpaOutboxRepository;
import com.youtube.contentidservice.infrastructure.persistence.entity.OutboxEventJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxEventPublisherImpl implements EventPublisher {
    private final JpaOutboxRepository outboxRepository;
    private final ObjectMapper objectMapper;

    @Override
    @Transactional
    public void publish(Object domainEvent) {
        try {
            String eventType = domainEvent.getClass().getSimpleName();
            String payload = objectMapper.writeValueAsString(domainEvent);
            
            // Extract aggregate info from event (simplified)
            String aggregateType = extractAggregateType(domainEvent);
            String aggregateId = extractAggregateId(domainEvent);

            OutboxEventJpaEntity outboxEvent = OutboxEventJpaEntity.builder()
                    .id(UUID.randomUUID())
                    .aggregateType(aggregateType)
                    .aggregateId(aggregateId)
                    .eventType(eventType)
                    .payload(payload)
                    .status("PENDING")
                    .createdAt(Instant.now())
                    .retryCount(0)
                    .build();

            outboxRepository.save(outboxEvent);
            log.debug("Outbox event created: {}", outboxEvent.getId());
        } catch (Exception e) {
            log.error("Error creating outbox event", e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }

    private String extractAggregateType(Object event) {
        // Simplified extraction - in production, use reflection or event metadata
        if (event.getClass().getSimpleName().contains("Fingerprint")) {
            return "Fingerprint";
        } else if (event.getClass().getSimpleName().contains("Match")) {
            return "Match";
        } else if (event.getClass().getSimpleName().contains("Claim")) {
            return "Claim";
        }
        return "Unknown";
    }

    private String extractAggregateId(Object event) {
        // Simplified - extract ID from event using reflection
        try {
            var idField = event.getClass().getDeclaredFields();
            for (var field : idField) {
                if (field.getName().toLowerCase().contains("id")) {
                    field.setAccessible(true);
                    Object id = field.get(event);
                    return id != null ? id.toString() : UUID.randomUUID().toString();
                }
            }
        } catch (Exception e) {
            log.warn("Could not extract aggregate ID from event", e);
        }
        return UUID.randomUUID().toString();
    }
}

