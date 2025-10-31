package com.youtube.analyticstelemetryservice.application.service;

import com.youtube.analyticstelemetryservice.application.dto.BatchEventRequest;
import com.youtube.analyticstelemetryservice.application.dto.BatchEventResponse;
import com.youtube.analyticstelemetryservice.application.dto.TelemetryEventRequest;
import com.youtube.analyticstelemetryservice.application.dto.TelemetryEventResponse;
import com.youtube.analyticstelemetryservice.application.mappers.TelemetryEventMapper;
import com.youtube.analyticstelemetryservice.domain.entities.TelemetryEvent;
import com.youtube.analyticstelemetryservice.domain.repositories.TelemetryEventRepository;
import com.youtube.analyticstelemetryservice.domain.services.DeadLetterQueue;
import com.youtube.analyticstelemetryservice.domain.services.EventPublisher;
import com.youtube.analyticstelemetryservice.domain.services.IdempotencyService;
import com.youtube.analyticstelemetryservice.domain.services.SchemaValidator;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

/**
 * Application service for telemetry event collection and processing.
 * Orchestrates domain services and infrastructure adapters.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TelemetryApplicationService {
    
    private final TelemetryEventMapper mapper;
    private final SchemaValidator schemaValidator;
    private final EventPublisher eventPublisher;
    private final DeadLetterQueue deadLetterQueue;
    private final IdempotencyService idempotencyService;
    private final TelemetryEventRepository eventRepository;
    private final TelemetryStatsService statsService;
    
    /**
     * Process a batch of events with idempotency, schema validation, and batching.
     * Applies rate limiting and retry via Resilience4j annotations.
     */
    @Retry(name = "eventPublisher")
    @RateLimiter(name = "eventCollection")
    @Transactional
    public CompletableFuture<BatchEventResponse> processBatch(BatchEventRequest request) {
        log.info("Processing batch of {} events", request.getEvents().size());
        
        String idempotencyKey = request.getIdempotencyKey();
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            if (idempotencyService.isProcessed(idempotencyKey)) {
                log.warn("Duplicate request with idempotency key: {}", idempotencyKey);
                return CompletableFuture.completedFuture(
                    BatchEventResponse.builder()
                        .totalReceived(request.getEvents().size())
                        .totalAccepted(0)
                        .totalRejected(request.getEvents().size())
                        .processedAt(Instant.now())
                        .build()
                );
            }
        }
        
        List<TelemetryEventResponse> results = new ArrayList<>();
        List<TelemetryEvent> validEvents = new ArrayList<>();
        AtomicInteger acceptedCount = new AtomicInteger(0);
        AtomicInteger rejectedCount = new AtomicInteger(0);
        Instant processedAt = Instant.now();
        
        // Process each event
        for (TelemetryEventRequest eventRequest : request.getEvents()) {
            try {
                // Map DTO to domain entity
                TelemetryEvent event = mapper.toDomain(eventRequest);
                
                // Validate domain rules
                event.validate();
                
                // Schema validation
                schemaValidator.validate(event);
                
                // Check idempotency (individual event level)
                if (eventRepository.existsById(event.getEventId().getValue())) {
                    log.debug("Duplicate event ID: {}", event.getEventId().getValue());
                    results.add(TelemetryEventResponse.builder()
                        .eventId(event.getEventId().getValue())
                        .status("rejected")
                        .message("Duplicate event ID")
                        .processedAt(processedAt)
                        .build());
                    rejectedCount.incrementAndGet();
                    continue;
                }
                
                validEvents.add(event);
                results.add(TelemetryEventResponse.builder()
                    .eventId(event.getEventId().getValue())
                    .status("accepted")
                    .message("Event accepted")
                    .processedAt(processedAt)
                    .build());
                acceptedCount.incrementAndGet();
                
            } catch (Exception e) {
                log.error("Failed to process event: {}", eventRequest, e);
                rejectedCount.incrementAndGet();
                results.add(TelemetryEventResponse.builder()
                    .eventId(eventRequest.getEventId())
                    .status("rejected")
                    .message("Validation failed: " + e.getMessage())
                    .processedAt(processedAt)
                    .build());
            }
        }
        
        // Save valid events to repository (optional - for audit/replay)
        if (!validEvents.isEmpty()) {
            eventRepository.saveAll(validEvents);
        }
        
        // Publish to Event Hubs asynchronously
        if (!validEvents.isEmpty()) {
            eventPublisher.publishBatch(validEvents)
                .thenRun(() -> {
                    log.info("Published {} events to Event Hubs", validEvents.size());
                    statsService.recordProcessed(validEvents.size());
                })
                .exceptionally(ex -> {
                    log.error("Failed to publish events to Event Hubs", ex);
                    // Send to DLQ
                    deadLetterQueue.sendBatchToDlq(validEvents, "Event Hubs publish failed", ex);
                    statsService.recordError(validEvents.size());
                    return null;
                });
        }
        
        // Mark idempotency key as processed
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            idempotencyService.markProcessed(idempotencyKey);
        }
        
        BatchEventResponse response = BatchEventResponse.builder()
            .totalReceived(request.getEvents().size())
            .totalAccepted(acceptedCount.get())
            .totalRejected(rejectedCount.get())
            .processedAt(processedAt)
            .results(results)
            .build();
        
        return CompletableFuture.completedFuture(response);
    }
    
    /**
     * Check if the service is healthy.
     */
    public boolean isHealthy() {
        return eventPublisher.isHealthy();
    }
}

