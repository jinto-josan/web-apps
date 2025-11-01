package com.youtube.common.domain.events.outbox;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.youtube.common.domain.persistence.entity.OutboxEvent;
import com.youtube.common.domain.services.tracing.TraceProvider;
import io.micrometer.tracing.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Background worker that dispatches outbox events to Azure Service Bus.
 * Polls the outbox periodically and publishes pending events.
 */
@Component
public class OutboxDispatcher {
    
    private static final Logger log = LoggerFactory.getLogger(OutboxDispatcher.class);
    
    private static final int DEFAULT_BATCH_SIZE = 100;
    private static final Duration BACKOFF_DURATION = Duration.ofSeconds(5);
    
    private final OutboxRepository outboxRepository;
    private final ServiceBusSenderClient serviceBusSender;
    private final TraceProvider traceProvider;
    private final String topicName;
    
    private volatile boolean enabled = true;
    
    public OutboxDispatcher(
        OutboxRepository outboxRepository,
        ServiceBusSenderClient serviceBusSender,
        TraceProvider traceProvider,
        String topicName
    ) {
        this.outboxRepository = outboxRepository;
        this.serviceBusSender = serviceBusSender;
        this.traceProvider = traceProvider;
        this.topicName = topicName;
    }
    
    /**
     * Scheduled task that polls for pending outbox events and dispatches them.
     * Runs every 5 seconds by default.
     */
    @Scheduled(fixedDelayString = "${outbox.dispatcher.interval:5000}")
    public void dispatchPendingEvents() {
        if (!enabled) {
            return;
        }
        
        Span span = traceProvider.startSpan("publish outbox batch");
        
        try {
            // Fetch pending events (uses SELECT ... FOR UPDATE SKIP LOCKED)
            var events = outboxRepository.fetchPendingBatch(DEFAULT_BATCH_SIZE);
            
            if (events.isEmpty()) {
                log.debug("No pending outbox events to dispatch");
                return;
            }
            
            log.info("Dispatching {} outbox events", events.size());
            
            // Publish each event
            for (OutboxEvent event : events) {
                try {
                    publishEvent(event);
                    outboxRepository.markDispatched(event.getId(), getBrokerMessageId(event));
                    log.debug("Dispatched event {} to Service Bus", event.getId());
                } catch (Exception e) {
                    log.error("Failed to dispatch event {}", event.getId(), e);
                    outboxRepository.markFailed(event.getId(), e.getMessage());
                }
            }
            
            log.info("Successfully dispatched {} outbox events", events.size());
            
        } catch (Exception e) {
            log.error("Error during outbox dispatch", e);
            traceProvider.endSpan(span, e);
        } finally {
            traceProvider.endSpan(span);
        }
    }
    
    private void publishEvent(OutboxEvent event) {
        ServiceBusMessage message = new ServiceBusMessage(event.getPayloadJson());
        
        // Set message properties
        message.setMessageId(event.getId());
        message.setCorrelationId(event.getCorrelationId());
        message.getApplicationProperties().put("traceparent", event.getTraceparent());
        message.getApplicationProperties().put("eventType", event.getEventType());
        message.getApplicationProperties().put("aggregateType", event.getAggregateType());
        message.getApplicationProperties().put("aggregateId", event.getAggregateId());
        
        // Set partition key if available
        if (event.getPartitionKey() != null) {
            message.setPartitionKey(event.getPartitionKey());
        }
        
        // Publish to Service Bus
        serviceBusSender.sendMessage(message);
    }
    
    private String getBrokerMessageId(OutboxEvent event) {
        // Service Bus message ID is set via message.setMessageId()
        // We can use the same ID or retrieve from response if needed
        return event.getId();
    }
    
    /**
     * Enables the dispatcher.
     */
    public void enable() {
        this.enabled = true;
    }
    
    /**
     * Disables the dispatcher.
     */
    public void disable() {
        this.enabled = false;
    }
}

