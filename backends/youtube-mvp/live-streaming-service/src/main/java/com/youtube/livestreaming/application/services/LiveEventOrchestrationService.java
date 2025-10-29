package com.youtube.livestreaming.application.services;

import com.youtube.livestreaming.application.dtos.CreateLiveEventRequest;
import com.youtube.livestreaming.domain.entities.LiveEvent;
import com.youtube.livestreaming.domain.ports.*;
import com.youtube.livestreaming.domain.valueobjects.*;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import io.github.resilience4j.timelimiter.annotation.TimeLimiter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.util.List;

/**
 * Orchestrates live event lifecycle operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class LiveEventOrchestrationService {
    
    private final LiveEventRepository repository;
    private final AmsClient amsClient;
    private final EventPublisher eventPublisher;
    private final IdempotencyService idempotencyService;
    
    @Transactional
    @CircuitBreaker(name = "live-event-service")
    @Retry(name = "live-event-service")
    @TimeLimiter(name = "live-event-service")
    public LiveEvent createLiveEvent(CreateLiveEventRequest request, String userId, String idempotencyKey) {
        log.info("Creating live event for user: {}, channel: {}", userId, request.getChannelId());
        
        // Handle idempotency
        if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
            var existingId = idempotencyService.processIdempotencyKey(idempotencyKey);
            if (existingId.isPresent()) {
                log.info("Idempotent request detected, returning existing live event: {}", existingId.get());
                return repository.findById(existingId.get()).orElseThrow();
            }
        }
        
        try {
            // Map request to domain
            var liveEvent = mapToEntity(request, userId);
            var savedEvent = repository.save(liveEvent);
            
            // Create in AMS asynchronously
            createAmsLiveEvent(savedEvent);
            
            // Store idempotency mapping
            if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
                idempotencyService.storeRequestId(idempotencyKey, savedEvent.getId());
            }
            
            // Publish domain events
            publishDomainEvents(savedEvent);
            
            log.info("Live event created successfully: {}", savedEvent.getId());
            return savedEvent;
            
        } catch (Exception e) {
            log.error("Failed to create live event", e);
            throw new RuntimeException("Failed to create live event: " + e.getMessage(), e);
        }
    }
    
    @Transactional
    @CircuitBreaker(name = "live-event-service")
    @Retry(name = "live-event-service")
    public LiveEvent startLiveEvent(String liveEventId, String userId) {
        log.info("Starting live event: {} for user: {}", liveEventId, userId);
        
        var liveEvent = repository.findByIdAndUserId(liveEventId, userId)
            .orElseThrow(() -> new RuntimeException("Live event not found"));
        
        if (liveEvent.isRunning()) {
            log.warn("Live event {} is already running", liveEventId);
            return liveEvent;
        }
        
        try {
            // Update domain state
            liveEvent.start();
            
            // Start in AMS
            amsClient.startLiveEvent(liveEvent.getAmsReference().getLiveEventName());
            
            var savedEvent = repository.save(liveEvent);
            publishDomainEvents(savedEvent);
            
            log.info("Live event started successfully: {}", liveEventId);
            return savedEvent;
            
        } catch (Exception e) {
            log.error("Failed to start live event", e);
            liveEvent.markFailed(e.getMessage());
            repository.save(liveEvent);
            throw new RuntimeException("Failed to start live event: " + e.getMessage(), e);
        }
    }
    
    @Transactional
    @CircuitBreaker(name = "live-event-service")
    @Retry(name = "live-event-service")
    public LiveEvent stopLiveEvent(String liveEventId, String userId) {
        log.info("Stopping live event: {} for user: {}", liveEventId, userId);
        
        var liveEvent = repository.findByIdAndUserId(liveEventId, userId)
            .orElseThrow(() -> new RuntimeException("Live event not found"));
        
        if (!liveEvent.isActive()) {
            log.warn("Live event {} is not active", liveEventId);
            return liveEvent;
        }
        
        try {
            // Update domain state
            liveEvent.stop();
            
            // Stop in AMS
            amsClient.stopLiveEvent(liveEvent.getAmsReference().getLiveEventName());
            
            var savedEvent = repository.save(liveEvent);
            publishDomainEvents(savedEvent);
            
            log.info("Live event stopped successfully: {}", liveEventId);
            return savedEvent;
            
        } catch (Exception e) {
            log.error("Failed to stop live event", e);
            liveEvent.markFailed(e.getMessage());
            repository.save(liveEvent);
            throw new RuntimeException("Failed to stop live event: " + e.getMessage(), e);
        }
    }
    
    @Transactional
    public void handleAmsCallback(String liveEventId, String state) {
        log.info("Processing AMS callback for live event: {}, state: {}", liveEventId, state);
        
        var liveEvent = repository.findById(liveEventId)
            .orElseThrow(() -> new RuntimeException("Live event not found: " + liveEventId));
        
        try {
            switch (state) {
                case "Running" -> liveEvent.confirmStarted(state);
                case "Stopped" -> liveEvent.confirmStopped();
                case "Archiving" -> liveEvent.archive();
                case "Archived" -> liveEvent.confirmArchived();
                case "StandBy" -> log.info("Live event {} is in standby", liveEventId);
                default -> log.warn("Unknown state: {}", state);
            }
            
            repository.save(liveEvent);
            
        } catch (Exception e) {
            log.error("Failed to process AMS callback", e);
            throw new RuntimeException("Failed to process AMS callback", e);
        }
    }
    
    public LiveEvent getLiveEvent(String liveEventId, String userId) {
        return repository.findByIdAndUserId(liveEventId, userId)
            .orElseThrow(() -> new RuntimeException("Live event not found"));
    }
    
    public List<LiveEvent> getLiveEventsByChannel(String channelId) {
        return repository.findByChannelId(channelId);
    }
    
    public List<LiveEvent> getLiveEventsByUser(String userId) {
        return repository.findByUserId(userId);
    }
    
    // Private helper methods
    
    private LiveEvent mapToEntity(CreateLiveEventRequest request, String userId) {
        var config = LiveEventConfiguration.builder()
            .name(request.getName())
            .description(request.getDescription())
            .channelId(request.getChannelId())
            .userId(userId)
            .dvrEnabled(request.getDvr() != null ? request.getDvr().getEnabled() : true)
            .dvrWindowInMinutes(request.getDvr() != null ? request.getDvr().getWindowInMinutes() : 120)
            .lowLatencyEnabled(request.getLowLatencyEnabled())
            .build();
        
        return new LiveEvent(
            generateId(),
            userId,
            request.getChannelId(),
            config
        );
    }
    
    private void createAmsLiveEvent(LiveEvent liveEvent) {
        try {
            var amsReference = amsClient.createLiveEvent(liveEvent.getId(), liveEvent.getConfiguration());
            liveEvent.assignAmsReference(amsReference);
            repository.save(liveEvent);
        } catch (Exception e) {
            log.error("Failed to create AMS live event", e);
            liveEvent.markFailed("AMS creation failed: " + e.getMessage());
            repository.save(liveEvent);
            throw e;
        }
    }
    
    private void publishDomainEvents(LiveEvent liveEvent) {
        var events = liveEvent.getDomainEvents();
        if (!events.isEmpty()) {
            eventPublisher.publishAll(events);
            liveEvent.clearDomainEvents();
            repository.save(liveEvent);
        }
    }
    
    private String generateId() {
        return "live-" + java.util.UUID.randomUUID().toString();
    }
}

