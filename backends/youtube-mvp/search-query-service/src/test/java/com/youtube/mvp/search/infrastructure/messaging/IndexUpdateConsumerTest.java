package com.youtube.mvp.search.infrastructure.messaging;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.youtube.mvp.search.application.service.SearchApplicationService;
import com.youtube.mvp.search.domain.model.SearchDocument;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class IndexUpdateConsumerTest {
    
    @Mock
    private ServiceBusReceiverClient receiverClient;
    
    @Mock
    private SearchApplicationService searchApplicationService;
    
    @Mock
    private ObjectMapper objectMapper;
    
    @InjectMocks
    private IndexUpdateConsumer consumer;
    
    @Test
    void processMessage_PublishedEvent_ShouldCallUpsert() throws Exception {
        // Given
        String eventJson = """
                {
                    "eventType": "PUBLISHED",
                    "videoId": "video-1",
                    "data": {
                        "videoId": "video-1",
                        "title": "Test Video",
                        "description": "Test Description"
                    }
                }
                """;
        
        ServiceBusReceivedMessage message = createMockMessage(eventJson);
        
        // When
        consumer.processMessage(message);
        
        // Then
        verify(searchApplicationService).handleIndexUpdate(any(SearchDocument.class), eq("PUBLISHED"));
        verify(receiverClient).completeMessage(message);
    }
    
    @Test
    void processMessage_DeletedEvent_ShouldCallDelete() throws Exception {
        // Given
        String eventJson = """
                {
                    "eventType": "DELETED",
                    "videoId": "video-1"
                }
                """;
        
        ServiceBusReceivedMessage message = createMockMessage(eventJson);
        
        // When
        consumer.processMessage(message);
        
        // Then
        verify(searchApplicationService).handleIndexUpdate(any(SearchDocument.class), eq("DELETED"));
    }
    
    private ServiceBusReceivedMessage createMockMessage(String body) {
        ServiceBusReceivedMessage message = mock(ServiceBusReceivedMessage.class);
        when(message.getBody()).thenReturn(body.getBytes(StandardCharsets.UTF_8));
        when(message.getMessageId()).thenReturn("msg-1");
        when(message.getSequenceNumber()).thenReturn(1L);
        when(message.getEnqueuedTime()).thenReturn(OffsetDateTime.now());
        return message;
    }
}
