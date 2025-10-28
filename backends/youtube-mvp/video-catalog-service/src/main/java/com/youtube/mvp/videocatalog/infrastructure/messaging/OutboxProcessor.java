package com.youtube.mvp.videocatalog.infrastructure.messaging;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.mvp.videocatalog.infrastructure.outbox.OutboxEvent;
import com.youtube.mvp.videocatalog.infrastructure.outbox.OutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

/**
 * Outbox processor that reads from Outbox and publishes to Service Bus.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class OutboxProcessor {
    
    private final OutboxRepository outboxRepository;
    private final ServiceBusSenderClient serviceBusSenderClient;
    private final ObjectMapper objectMapper;
    
    /**
     * Processes pending outbox events every 5 seconds.
     */
    @Scheduled(fixedRate = 5000)
    public void processPendingEvents() {
        log.debug("Processing pending outbox events");
        
        List<OutboxEvent> pending = outboxRepository.findAll()
                .stream()
                .filter(e -> "PENDING".equals(e.getStatus()))
                .limit(100) // Process batch
                .toList();
        
        for (OutboxEvent event : pending) {
            try {
                publishToServiceBus(event);
                
                event.setStatus("PROCESSED");
                event.setProcessedAt(Instant.now());
                outboxRepository.save(event);
                
                log.debug("Processed outbox event: {}", event.getEventId());
                
            } catch (Exception e) {
                log.error("Failed to process outbox event: {}", event.getEventId(), e);
                
                event.setRetryCount(event.getRetryCount() + 1);
                if (event.getRetryCount() >= 3) {
                    event.setStatus("FAILED");
                }
                outboxRepository.save(event);
            }
        }
    }
    
    private void publishToServiceBus(OutboxEvent event) {
        String topic = "video-" + event.getEventType().toLowerCase();
        
        ServiceBusMessage message = new ServiceBusMessage(event.getPayload())
                .setMessageId(event.getEventId())
                .setSubject(event.getEventType())
                .addHeader("eventType", event.getEventType())
                .addHeader("aggregateType", event.getAggregateType())
                .addHeader("aggregateId", event.getAggregateId());
        
        serviceBusSenderClient.sendMessage(message);
        log.debug("Published message to topic: {}", topic);
    }
}

