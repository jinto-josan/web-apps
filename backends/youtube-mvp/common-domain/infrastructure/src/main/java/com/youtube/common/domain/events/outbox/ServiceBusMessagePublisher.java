package com.youtube.common.domain.events.outbox;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.youtube.common.domain.persistence.entity.OutboxEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Azure Service Bus implementation of MessagePublisher.
 */
public class ServiceBusMessagePublisher implements MessagePublisher {
    
    private static final Logger log = LoggerFactory.getLogger(ServiceBusMessagePublisher.class);
    
    private final ServiceBusSenderClient sender;
    
    public ServiceBusMessagePublisher(ServiceBusSenderClient sender) {
        this.sender = sender;
    }
    
    @Override
    public void publish(OutboxEvent event) {
        try {
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
            sender.sendMessage(message);
            log.debug("Published event {} to Service Bus", event.getId());
        } catch (Exception e) {
            throw new MessagePublishException("Failed to publish event to Service Bus: " + event.getId(), e);
        }
    }
    
    @Override
    public String getBrokerMessageId(OutboxEvent event) {
        // Service Bus message ID is set via message.setMessageId()
        // We use the same ID as the outbox event ID for consistency
        return event.getId();
    }
}

