package com.youtube.analyticstelemetryservice.infrastructure.adapters.eventhubs;

import com.azure.core.util.BinaryData;
import com.azure.messaging.eventhubs.EventData;
import com.azure.messaging.eventhubs.EventDataBatch;
import com.azure.messaging.eventhubs.EventHubProducerClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.analyticstelemetryservice.domain.entities.TelemetryEvent;
import com.youtube.analyticstelemetryservice.domain.services.EventPublisher;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Infrastructure adapter for publishing events to Azure Event Hubs.
 * Implements batching, retry, and circuit breaker patterns.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EventHubsEventPublisher implements EventPublisher {
    
    private final EventHubProducerClient eventHubProducerClient;
    private final ObjectMapper objectMapper;
    
    @Value("${azure.eventhubs.batch-size:100}")
    private int batchSize;
    
    @Value("${azure.eventhubs.max-batch-size:1048576}") // 1MB
    private int maxBatchSizeBytes;
    
    private final ExecutorService executorService = Executors.newFixedThreadPool(10);
    private volatile boolean healthy = true;
    
    @Override
    @Retry(name = "eventPublisher")
    @CircuitBreaker(name = "eventPublisher", fallbackMethod = "publishFallback")
    public CompletableFuture<Void> publish(TelemetryEvent event) {
        return publishBatch(List.of(event));
    }
    
    @Override
    @Retry(name = "eventPublisher")
    @CircuitBreaker(name = "eventPublisher", fallbackMethod = "publishBatchFallback")
    public CompletableFuture<Void> publishBatch(List<TelemetryEvent> events) {
        if (events == null || events.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }
        
        if (!isProducerAvailable()) {
            log.warn("Event Hubs producer client not available. Cannot publish events.");
            return CompletableFuture.failedFuture(new IllegalStateException("Event Hubs producer not available"));
        }
        
        return CompletableFuture.runAsync(() -> {
            try {
                List<List<TelemetryEvent>> batches = partitionIntoBatches(events, batchSize);
                
                for (List<TelemetryEvent> batch : batches) {
                    EventDataBatch eventDataBatch = eventHubProducerClient.createBatch();
                    
                    for (TelemetryEvent event : batch) {
                        try {
                            String json = serializeEvent(event);
                            EventData eventData = new EventData(BinaryData.fromString(json));
                            
                            // Add correlation ID as a property
                            if (event.getCorrelationId() != null) {
                                eventData.getProperties().put("correlationId", event.getCorrelationId());
                            }
                            eventData.getProperties().put("eventType", event.getEventType().getValue());
                            eventData.getProperties().put("eventSource", event.getEventSource().getValue());
                            
                            if (eventDataBatch.tryAdd(eventData)) {
                                // Successfully added to batch
                            } else {
                                // Batch is full, send current batch and create a new one
                                if (eventDataBatch.getCount() > 0) {
                                    eventHubProducerClient.send(eventDataBatch);
                                    log.debug("Sent batch of {} events to Event Hubs", eventDataBatch.getCount());
                                }
                                eventDataBatch = eventHubProducerClient.createBatch();
                                
                                // Try adding again to the new batch
                                if (!eventDataBatch.tryAdd(eventData)) {
                                    log.error("Event too large to fit in batch: {}", event.getEventId().getValue());
                                }
                            }
                        } catch (Exception e) {
                            log.error("Failed to serialize event: {}", event.getEventId().getValue(), e);
                        }
                    }
                    
                    // Send remaining events in the batch
                    if (eventDataBatch.getCount() > 0) {
                        eventHubProducerClient.send(eventDataBatch);
                        log.debug("Sent final batch of {} events to Event Hubs", eventDataBatch.getCount());
                    }
                }
                
                log.info("Successfully published {} events to Event Hubs", events.size());
                healthy = true;
                
            } catch (Exception e) {
                log.error("Failed to publish events to Event Hubs", e);
                healthy = false;
                throw new RuntimeException("Failed to publish events to Event Hubs", e);
            }
        }, executorService);
    }
    
    @Override
    public boolean isHealthy() {
        return healthy && eventHubProducerClient != null;
    }
    
    // Handle null producer client gracefully
    private boolean isProducerAvailable() {
        return eventHubProducerClient != null;
    }
    
    private String serializeEvent(TelemetryEvent event) {
        try {
            return objectMapper.writeValueAsString(event);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize event", e);
        }
    }
    
    private List<List<TelemetryEvent>> partitionIntoBatches(List<TelemetryEvent> events, int batchSize) {
        List<List<TelemetryEvent>> batches = new ArrayList<>();
        for (int i = 0; i < events.size(); i += batchSize) {
            batches.add(events.subList(i, Math.min(i + batchSize, events.size())));
        }
        return batches;
    }
    
    // Fallback methods for circuit breaker
    public CompletableFuture<Void> publishFallback(TelemetryEvent event, Exception e) {
        log.error("Circuit breaker opened or retry exhausted for event: {}", event.getEventId().getValue(), e);
        return CompletableFuture.failedFuture(e);
    }
    
    public CompletableFuture<Void> publishBatchFallback(List<TelemetryEvent> events, Exception e) {
        log.error("Circuit breaker opened or retry exhausted for batch of {} events", events.size(), e);
        return CompletableFuture.failedFuture(e);
    }
}

