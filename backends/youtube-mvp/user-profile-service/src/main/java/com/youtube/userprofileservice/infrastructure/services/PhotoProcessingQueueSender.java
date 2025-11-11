package com.youtube.userprofileservice.infrastructure.services;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.common.domain.services.correlation.CorrelationContext;
import com.youtube.userprofileservice.domain.events.PhotoUploadedEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * Service for sending photo upload completion messages to the processing queue.
 * This is called after a photo is uploaded to trigger virus scanning and compression.
 */
@Slf4j
@Service
@ConditionalOnProperty(name = "app.photo-processing.enabled", havingValue = "true", matchIfMissing = false)
public class PhotoProcessingQueueSender {
    
    private final ServiceBusSenderClient senderClient;
    private final ObjectMapper objectMapper;
    
    public PhotoProcessingQueueSender(
            @Value("${azure.servicebus.connection-string:}") String serviceBusConnectionString,
            @Value("${azure.servicebus.queue-name:photo-processing}") String queueName,
            ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
        
        if (serviceBusConnectionString == null || serviceBusConnectionString.isBlank()) {
            log.warn("Service Bus connection string not configured - photo processing queue sender disabled");
            this.senderClient = null;
        } else {
            this.senderClient = new ServiceBusClientBuilder()
                    .connectionString(serviceBusConnectionString)
                    .sender()
                    .queueName(queueName)
                    .buildClient();
        }
    }
    
    /**
     * Sends a message to the photo processing queue after upload completion.
     * 
     * @param accountId the account ID
     * @param blobName the blob name
     * @param containerName the container name
     * @param contentType the content type
     * @param fileSizeBytes the file size in bytes
     */
    public void sendPhotoUploadedMessage(String accountId, String blobName, String containerName,
                                        String contentType, long fileSizeBytes) {
        String correlationId = CorrelationContext.getCorrelationId().orElse("unknown");
        log.info("Sending photo uploaded message to queue - accountId: {}, blobName: {}, correlationId: {}",
                accountId, blobName, correlationId);
        
        if (senderClient == null) {
            log.warn("Service Bus sender not configured - photo processing message not sent");
            return;
        }
        
        try {
            // Create domain event
            PhotoUploadedEvent event = new PhotoUploadedEvent(
                    accountId,
                    blobName,
                    containerName,
                    contentType,
                    fileSizeBytes
            );
            
            String messageBody = objectMapper.writeValueAsString(event);
            
            // Create Service Bus message with correlation ID and event metadata
            ServiceBusMessage serviceBusMessage = new ServiceBusMessage(messageBody);
            serviceBusMessage.setMessageId(event.getEventId());
            serviceBusMessage.setCorrelationId(correlationId);
            serviceBusMessage.getApplicationProperties().put("correlationId", correlationId);
            serviceBusMessage.getApplicationProperties().put("eventType", event.getEventType());
            serviceBusMessage.getApplicationProperties().put("accountId", accountId);
            serviceBusMessage.getApplicationProperties().put("blobName", blobName);
            
            senderClient.sendMessage(serviceBusMessage);
            
            log.info("Photo uploaded event sent successfully - accountId: {}, blobName: {}, eventId: {}, correlationId: {}",
                    accountId, blobName, event.getEventId(), correlationId);
        } catch (Exception e) {
            log.error("Failed to send photo uploaded event - accountId: {}, blobName: {}, correlationId: {}",
                    accountId, blobName, correlationId, e);
            throw new RuntimeException("Failed to send photo processing event", e);
        }
    }
}

