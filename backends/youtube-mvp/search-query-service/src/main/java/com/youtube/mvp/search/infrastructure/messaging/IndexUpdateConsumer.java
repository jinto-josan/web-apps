package com.youtube.mvp.search.infrastructure.messaging;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.mvp.search.application.service.SearchApplicationService;
import com.youtube.mvp.search.domain.model.SearchDocument;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Service Bus consumer for index update events.
 * Listens for video.published, video.updated, video.deleted events.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class IndexUpdateConsumer implements CommandLineRunner {
    
    private final ServiceBusReceiverClient receiverClient;
    private final SearchApplicationService searchApplicationService;
    private final ObjectMapper objectMapper;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private volatile boolean running = false;
    
    @Override
    public void run(String... args) throws Exception {
        log.info("Starting index update consumer");
        running = true;
        
        executorService.submit(() -> {
            while (running) {
                try {
                    ServiceBusReceivedMessage message = receiverClient.receiveMessage(Duration.ofSeconds(30));
                    
                    if (message != null) {
                        processMessage(message);
                        receiverClient.completeMessage(message);
                    }
                } catch (Exception e) {
                    log.error("Error consuming message", e);
                }
            }
        });
    }
    
    public void stop() {
        log.info("Stopping index update consumer");
        running = false;
        executorService.shutdown();
    }
    
    private void processMessage(ServiceBusReceivedMessage message) {
        try {
            String messageBody = message.getBody().toString();
            Map<String, Object> event = objectMapper.readValue(messageBody, Map.class);
            
            String eventType = (String) event.get("eventType");
            String videoId = (String) event.get("videoId");
            
            log.info("Processing index update: eventType={}, videoId={}", eventType, videoId);
            
            if ("DELETED".equals(eventType)) {
                // Delete from index
                searchApplicationService.handleIndexUpdate(
                        SearchDocument.builder().videoId(videoId).build(),
                        "DELETED"
                );
            } else {
                // Upsert to index
                SearchDocument document = buildSearchDocument(event);
                searchApplicationService.handleIndexUpdate(document, eventType);
            }
            
        } catch (Exception e) {
            log.error("Failed to process index update message", e);
            throw new RuntimeException("Message processing failed", e);
        }
    }
    
    @SuppressWarnings("unchecked")
    private SearchDocument buildSearchDocument(Map<String, Object> event) {
        Map<String, Object> data = (Map<String, Object>) event.get("data");
        
        return SearchDocument.builder()
                .videoId((String) data.get("videoId"))
                .title((String) data.get("title"))
                .description((String) data.get("description"))
                .channelName((String) data.get("channelName"))
                .channelId((String) data.get("channelId"))
                .category((String) data.get("category"))
                .tags((String) data.get("tags"))
                .viewCount(getLongValue(data.get("viewCount")))
                .likeCount(getLongValue(data.get("likeCount")))
                .duration(getLongValue(data.get("duration")))
                .thumbnailUrl((String) data.get("thumbnailUrl"))
                .publishedAt(getLongValue(data.get("publishedAt")))
                .language((String) data.get("language"))
                .quality((Integer) data.get("quality"))
                .build();
    }
    
    private Long getLongValue(Object value) {
        if (value == null) return null;
        if (value instanceof Long) return (Long) value;
        if (value instanceof Integer) return ((Integer) value).longValue();
        if (value instanceof Number) return ((Number) value).longValue();
        return null;
    }
}
