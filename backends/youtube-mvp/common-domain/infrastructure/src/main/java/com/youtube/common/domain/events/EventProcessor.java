package com.youtube.common.domain.events;

import com.azure.messaging.servicebus.ServiceBusReceivedMessage;
import com.youtube.common.domain.core.DomainEvent;
import com.youtube.common.domain.core.UnitOfWork;
import com.youtube.common.domain.services.correlation.CorrelationContext;
import com.youtube.common.domain.events.inbox.InboxRepository;
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
                // Get event type from message properties for proper deserialization
                String eventType = (String) message.getApplicationProperties().get("eventType");
                
                // Deserialize event
                DomainEvent event = deserializeEvent(message.getBody().toString(), eventType);
                
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
    
    private DomainEvent deserializeEvent(String payload, String eventType) {
        try {
            // If eventType is provided, try to resolve the concrete event class
            if (eventType != null && !eventType.isBlank()) {
                Class<? extends DomainEvent> eventClass = resolveEventClass(eventType);
                if (eventClass != null) {
                    return objectMapper.readValue(payload, eventClass);
                }
                
                // If not found in common-domain, try to deserialize using ObjectMapper's
                // registered subtypes. ObjectMapper will use the registered subtypes
                // if the JSON contains type information or if we use TypeReference.
                // For now, try to deserialize as the registered subtype.
                // Note: This requires services to register subtypes via ObjectMapper.registerSubtypes()
            }
            
            // Fallback: Try to deserialize as generic DomainEvent
            // This will work if ObjectMapper has registered subtypes and can infer the type
            // from the JSON structure or if @JsonTypeInfo is present on DomainEvent
            try {
                return objectMapper.readValue(payload, DomainEvent.class);
            } catch (Exception e) {
                // If generic deserialization fails, try using a TypeReference approach
                // or throw a more descriptive error
                throw new RuntimeException(
                    "Failed to deserialize event. EventType: " + eventType + 
                    ". Make sure the event class is registered via ObjectMapper.registerSubtypes() " +
                    "or add @JsonTypeInfo to DomainEvent base class.", e);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException("Failed to deserialize event: " + eventType, e);
        }
    }
    
    /**
     * Resolves the concrete event class from event type.
     * Uses reflection to load classes from common-domain event contracts.
     * For service-specific events, services should register them via ObjectMapper.registerSubtypes()
     * and the ObjectMapper will handle deserialization via registered subtypes.
     * 
     * @param eventType the event type string
     * @return the event class, or null if not found
     */
    @SuppressWarnings("unchecked")
    private Class<? extends DomainEvent> resolveEventClass(String eventType) {
        try {
            // Try to resolve from common-domain event contracts first
            String className = switch (eventType) {
                case "user.created" -> "com.youtube.common.domain.events.UserCreatedEvent";
                case "channel.created" -> "com.youtube.common.domain.events.ChannelCreatedEvent";
                case "video.published" -> "com.youtube.common.domain.events.VideoPublishedEvent";
                default -> null;
            };
            
            if (className != null) {
                Class<?> clazz = Class.forName(className);
                if (DomainEvent.class.isAssignableFrom(clazz)) {
                    return (Class<? extends DomainEvent>) clazz;
                }
            }
            
            // For service-specific events, try to load by fully qualified class name using reflection
            // This allows services to define events without modifying common-domain
            // Services can register their event classes via ObjectMapper.registerSubtypes()
            // and the fallback deserialization will use those registered subtypes
            // 
            // Example mappings for service-specific events (can be extended):
            String serviceSpecificClassName = switch (eventType) {
                case "photo.uploaded" -> "com.youtube.userprofileservice.domain.events.PhotoUploadedEvent";
                default -> null;
            };
            
            if (serviceSpecificClassName != null) {
                try {
                    Class<?> clazz = Class.forName(serviceSpecificClassName);
                    if (DomainEvent.class.isAssignableFrom(clazz)) {
                        return (Class<? extends DomainEvent>) clazz;
                    }
                } catch (ClassNotFoundException e) {
                    log.debug("Service-specific event class not found: {}", serviceSpecificClassName);
                }
            }
            
            return null;
        } catch (ClassNotFoundException e) {
            log.debug("Event class not found for type: {} (will try ObjectMapper subtypes)", eventType);
            return null;
        }
    }
}

