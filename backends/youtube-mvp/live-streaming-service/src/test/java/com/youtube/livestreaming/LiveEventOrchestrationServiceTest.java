package com.youtube.livestreaming;

import com.youtube.livestreaming.application.services.LiveEventOrchestrationService;
import com.youtube.livestreaming.domain.entities.LiveEvent;
import com.youtube.livestreaming.domain.ports.AmsClient;
import com.youtube.livestreaming.domain.ports.EventPublisher;
import com.youtube.livestreaming.domain.ports.IdempotencyService;
import com.youtube.livestreaming.domain.ports.LiveEventRepository;
import com.youtube.livestreaming.domain.valueobjects.AmsLiveEventReference;
import com.youtube.livestreaming.domain.valueobjects.LiveEventConfiguration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class LiveEventOrchestrationServiceTest {
    
    @Mock
    private LiveEventRepository repository;
    
    @Mock
    private AmsClient amsClient;
    
    @Mock
    private EventPublisher eventPublisher;
    
    @Mock
    private IdempotencyService idempotencyService;
    
    private LiveEventOrchestrationService service;
    
    @BeforeEach
    void setUp() {
        service = new LiveEventOrchestrationService(repository, amsClient, eventPublisher, idempotencyService);
    }
    
    @Test
    void shouldCreateLiveEvent() {
        // Given
        var config = LiveEventConfiguration.builder()
            .name("Test Event")
            .channelId("channel-123")
            .userId("user-456")
            .dvrEnabled(true)
            .build();
        
        var liveEvent = new LiveEvent("live-1", "user-456", "channel-123", config);
        
        var amsReference = AmsLiveEventReference.builder()
            .liveEventId("ams-live-1")
            .liveEventName("live-1")
            .accountName("test-account")
            .resourceGroupName("test-rg")
            .ingestUrl("rtmp://test.ingest")
            .previewUrl("https://test.preview")
            .state("Stopped")
            .build();
        
        when(amsClient.createLiveEvent(any(), any())).thenReturn(amsReference);
        when(repository.save(any())).thenReturn(liveEvent);
        when(idempotencyService.processIdempotencyKey(any())).thenReturn(Optional.empty());
        
        // When - this test would need proper request DTO creation
        // var request = new CreateLiveEventRequest();
        // var created = service.createLiveEvent(request, "user-456", "idempotency-key");
        
        // Then
        verify(repository, times(2)).save(any());
    }
}

