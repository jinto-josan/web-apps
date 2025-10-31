package com.youtube.analyticstelemetryservice.infrastructure.adapters.blob;

import com.azure.core.util.BinaryData;
import com.azure.storage.blob.BlobClient;
import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.BlobServiceClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.analyticstelemetryservice.domain.entities.TelemetryEvent;
import com.youtube.analyticstelemetryservice.domain.services.DeadLetterQueue;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Infrastructure adapter for dead letter queue using Azure Blob Storage.
 * Failed events are stored in Blob Storage for later analysis.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BlobDeadLetterQueue implements DeadLetterQueue {
    
    private final ObjectMapper objectMapper;
    
    @Value("${azure.storage.connection-string:}")
    private String storageConnectionString;
    
    @Value("${azure.storage.dlq-container-name:telemetry-dlq}")
    private String dlqContainerName;
    
    private BlobServiceClient blobServiceClient;
    private BlobContainerClient dlqContainerClient;
    
    @PostConstruct
    public void initialize() {
        if (storageConnectionString != null && !storageConnectionString.isBlank()) {
            blobServiceClient = new BlobServiceClientBuilder()
                .connectionString(storageConnectionString)
                .buildClient();
            
            dlqContainerClient = blobServiceClient.getBlobContainerClient(dlqContainerName);
            if (!dlqContainerClient.exists()) {
                dlqContainerClient.create();
                log.info("Created DLQ container: {}", dlqContainerName);
            }
        }
    }
    
    @Override
    public void sendToDlq(TelemetryEvent event, String errorMessage, Throwable exception) {
        if (blobServiceClient == null) {
            log.warn("Blob Storage not configured. Cannot send to DLQ. Event: {}", event.getEventId().getValue());
            return;
        }
        
        try {
            initialize();
            
            Map<String, Object> dlqRecord = createDlqRecord(event, errorMessage, exception);
            String json = objectMapper.writeValueAsString(dlqRecord);
            
            String blobName = generateBlobName(event);
            BlobClient blobClient = dlqContainerClient.getBlobClient(blobName);
            blobClient.upload(BinaryData.fromString(json), true);
            
            log.info("Sent event to DLQ: {}", blobName);
            
        } catch (Exception e) {
            log.error("Failed to send event to DLQ: {}", event.getEventId().getValue(), e);
        }
    }
    
    @Override
    public void sendBatchToDlq(List<TelemetryEvent> events, String errorMessage, Throwable exception) {
        for (TelemetryEvent event : events) {
            sendToDlq(event, errorMessage, exception);
        }
    }
    
    private Map<String, Object> createDlqRecord(TelemetryEvent event, String errorMessage, Throwable exception) {
        Map<String, Object> record = new HashMap<>();
        record.put("eventId", event.getEventId().getValue());
        record.put("eventType", event.getEventType().getValue());
        record.put("eventSource", event.getEventSource().getValue());
        record.put("timestamp", event.getTimestamp().toString());
        record.put("userId", event.getUserId());
        record.put("sessionId", event.getSessionId());
        record.put("properties", event.getProperties());
        record.put("errorMessage", errorMessage);
        record.put("exceptionType", exception != null ? exception.getClass().getName() : null);
        record.put("exceptionMessage", exception != null ? exception.getMessage() : null);
        record.put("dlqTimestamp", Instant.now().toString());
        return record;
    }
    
    private String generateBlobName(TelemetryEvent event) {
        String date = DateTimeFormatter.ofPattern("yyyy/MM/dd").format(event.getTimestamp());
        String timestamp = DateTimeFormatter.ofPattern("HHmmss").format(Instant.now());
        return String.format("%s/%s-%s.json", date, event.getEventId().getValue(), timestamp);
    }
}

