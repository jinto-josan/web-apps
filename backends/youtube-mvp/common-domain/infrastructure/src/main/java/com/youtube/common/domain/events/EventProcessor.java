package com.youtube.common.domain.events;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.youtube.common.domain.core.DomainEvent;
import com.youtube.common.domain.core.UnitOfWork;
import com.youtube.common.domain.services.correlation.CorrelationContext;
import com.youtube.common.domain.events.inbox.InboxRepository;
import com.youtube.common.domain.events.outbox.OutboxRepository;
import com.youtube.common.domain.repository.Repository;
import com.youtube.common.domain.services.tracing.TraceProvider;
import io.micrometer.tracing.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Service for processing events from Azure Service Bus with inbox idempotency.
 * Implements the inbox pattern to ensure exactly-once processing.
 */
@Component
public class EventProcessor {
    
    private static final Logger log = LoggerFactory.getLogger(EventProcessor.class);
    
    private final InboxRepository inboxRepository;
    private final EventRouter eventRouter;
    private final UnitOfWork unitOfWork;
    private final TraceProvider traceProvider;
    private final ObjectMapper objectMapper;
    
    public EventProcessor(
        InboxRepository inboxRepository,
        EventRouter eventRouter,
        UnitOfWork unitOfWork,
        TraceProvider traceProvider,
        ObjectMapper objectMapper
    ) {
        this.inboxRepository = inboxRepository;
        this.eventRouter = eventRouter;
        this.unitOfWork = unitOfWork;
        this.traceProvider = traceProvider;
        this.objectMapper = objectMapper;
    }
    
    /**
     * Processes a message from Azure Service Bus.
     * 
     * @param message the Service Bus message
     */
    public void process(ServiceBusReceivedMessage message) {
        String messageId = message.getMessageId();
        String correlationId = message.getCorrelationId();
        String traceparent = (String) message.getApplicationProperties().get("traceparent");
        
        Span span = traceProvider.startSpan("handle event", traceparent);
        
        try {
            // Set correlation context
            if (correlationId != null) {
                CorrelationContext.setCorrelationId(correlationId);
            }
            
            // Check inbox for idempotency
            boolean started = inboxRepository.beginProcess(messageId);
            
            if (!started) {
                // Already processed - duplicate message
                log.debug("Message {} already processed, skipping", messageId);
                return;
            }
            
            // Begin transaction
            unitOfWork.begin();
            
            try {
                // Deserialize event
                DomainEvent event = deserializeEvent(message.getBody().toString());
                
                // Resolve handler
                EventRouter.EventHandler<DomainEvent> handler = eventRouter.resolveHandler(event);
                if (handler == null) {
                    throw new IllegalArgumentException("No handler found for event type: " + event.getEventType());
                }
                
                // Handle event
                handler.handle(event, correlationId);
                
                // Commit transaction
                unitOfWork.commit();
                
                // Mark as processed
                inboxRepository.markProcessed(messageId);
                
                log.info("Successfully processed event {} with message ID {}", event.getEventType(), messageId);
                
            } catch (Exception e) {
                // Rollback transaction
                unitOfWork.rollback(e);
                
                // Record failure
                inboxRepository.recordFailure(messageId, e.getMessage());
                
                throw e;
            }
            
        } catch (Exception e) {
            log.error("Failed to process event with message ID {}", messageId, e);
            traceProvider.endSpan(span, e);
            throw e;
        } finally {
            traceProvider.endSpan(span);
            CorrelationContext.clear();
        }
    }
    
    private DomainEvent deserializeEvent(String payload) {
        try {
            // This is a simplified implementation
            // In a real system, you'd need proper event type resolution
            return objectMapper.readValue(payload, DomainEvent.class);
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize event", e);
        }
    }
}

