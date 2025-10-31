package com.youtube.analyticstelemetryservice.application;

import com.youtube.analyticstelemetryservice.application.dto.BatchEventRequest;
import com.youtube.analyticstelemetryservice.application.dto.TelemetryEventRequest;
import com.youtube.analyticstelemetryservice.application.mappers.TelemetryEventMapper;
import com.youtube.analyticstelemetryservice.application.service.TelemetryApplicationService;
import com.youtube.analyticstelemetryservice.application.service.TelemetryStatsService;
import com.youtube.analyticstelemetryservice.domain.repositories.TelemetryEventRepository;
import com.youtube.analyticstelemetryservice.domain.services.DeadLetterQueue;
import com.youtube.analyticstelemetryservice.domain.services.EventPublisher;
import com.youtube.analyticstelemetryservice.domain.services.IdempotencyService;
import com.youtube.analyticstelemetryservice.domain.services.SchemaValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CompletableFuture;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TelemetryApplicationServiceTest {
    
    @Mock
    private TelemetryEventMapper mapper;
    
    @Mock
    private SchemaValidator schemaValidator;
    
    @Mock
    private EventPublisher eventPublisher;
    
    @Mock
    private DeadLetterQueue deadLetterQueue;
    
    @Mock
    private IdempotencyService idempotencyService;
    
    @Mock
    private TelemetryEventRepository eventRepository;
    
    @Mock
    private TelemetryStatsService statsService;
    
    @InjectMocks
    private TelemetryApplicationService applicationService;
    
    @BeforeEach
    void setUp() {
        when(eventPublisher.isHealthy()).thenReturn(true);
        when(eventPublisher.publishBatch(any())).thenReturn(CompletableFuture.completedFuture(null));
        when(idempotencyService.isProcessed(anyString())).thenReturn(false);
        when(eventRepository.existsById(anyString())).thenReturn(false);
    }
    
    @Test
    void shouldProcessValidBatch() {
        // Given
        TelemetryEventRequest request = createValidEventRequest();
        BatchEventRequest batchRequest = new BatchEventRequest();
        batchRequest.setEvents(Collections.singletonList(request));
        
        // When
        CompletableFuture<com.youtube.analyticstelemetryservice.application.dto.BatchEventResponse> future = 
            applicationService.processBatch(batchRequest);
        
        // Then
        com.youtube.analyticstelemetryservice.application.dto.BatchEventResponse response = future.join();
        assertThat(response.getTotalReceived()).isEqualTo(1);
        assertThat(response.getTotalAccepted()).isGreaterThanOrEqualTo(0);
    }
    
    @Test
    void shouldRejectDuplicateIdempotencyKey() {
        // Given
        BatchEventRequest batchRequest = new BatchEventRequest();
        batchRequest.setEvents(Collections.singletonList(createValidEventRequest()));
        batchRequest.setIdempotencyKey("duplicate-key");
        
        when(idempotencyService.isProcessed("duplicate-key")).thenReturn(true);
        
        // When
        CompletableFuture<com.youtube.analyticstelemetryservice.application.dto.BatchEventResponse> future = 
            applicationService.processBatch(batchRequest);
        
        // Then
        com.youtube.analyticstelemetryservice.application.dto.BatchEventResponse response = future.join();
        assertThat(response.getTotalRejected()).isEqualTo(1);
        verify(eventPublisher, never()).publishBatch(any());
    }
    
    private TelemetryEventRequest createValidEventRequest() {
        TelemetryEventRequest request = new TelemetryEventRequest();
        request.setEventType("video.view");
        request.setEventSource("web");
        request.setTimestamp(Instant.now());
        return request;
    }
}

