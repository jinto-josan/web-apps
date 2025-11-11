package com.youtube.userprofileservice.infrastructure.messaging;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.azure.messaging.servicebus.ServiceBusReceiverClient;
import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.youtube.common.domain.events.EventProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import java.time.Duration;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Listens to the photo processing queue and processes uploaded photos using EventProcessor.
 * EventProcessor provides inbox idempotency, transaction management, and event routing.
 */
@Slf4j
@Component
@ConditionalOnProperty(name = "app.photo-processing.enabled", havingValue = "true", matchIfMissing = false)
public class PhotoProcessingQueueListener {
    
    private final ServiceBusReceiverClient receiverClient;
    private final EventProcessor eventProcessor;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private volatile boolean running = false;
    
    public PhotoProcessingQueueListener(
            @Value("${azure.servicebus.connection-string:}") String serviceBusConnectionString,
            @Value("${azure.servicebus.queue-name:photo-processing}") String queueName,
            EventProcessor eventProcessor) {
        this.eventProcessor = eventProcessor;
        
        if (serviceBusConnectionString == null || serviceBusConnectionString.isBlank()) {
            log.warn("Service Bus connection string not configured - photo processing queue listener disabled");
            this.receiverClient = null;
        } else {
            // Initialize Service Bus receiver
            this.receiverClient = new ServiceBusClientBuilder()
                    .connectionString(serviceBusConnectionString)
                    .receiver()
                    .queueName(queueName)
                    .buildClient();
        }
    }
    
    @PostConstruct
    public void start() {
        if (receiverClient == null) {
            log.info("Photo processing queue listener not started - Service Bus not configured");
            return;
        }
        
        log.info("Starting photo processing queue listener");
        running = true;
        
        executorService.submit(() -> {
            while (running) {
                try {
                    // Receive messages (up to 1 at a time)
                    Iterable<ServiceBusReceivedMessage> messages = receiverClient.receiveMessages(1, Duration.ofSeconds(30));
                    
                    for (ServiceBusReceivedMessage message : messages) {
                        try {
                            // Use EventProcessor for inbox idempotency, transaction management, and event routing
                            eventProcessor.process(message);
                            receiverClient.complete(message);
                        } catch (Exception e) {
                            log.error("Error processing message, abandoning", e);
                            receiverClient.abandon(message);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error receiving messages from queue", e);
                }
            }
        });
    }
    
    @PreDestroy
    public void stop() {
        log.info("Stopping photo processing queue listener");
        running = false;
        executorService.shutdown();
        if (receiverClient != null) {
            receiverClient.close();
        }
    }
    
}

