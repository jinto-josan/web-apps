package com.youtube.channelservice.infrastructure.messaging;

import com.youtube.channelservice.domain.services.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.messaging.Message;
import org.springframework.messaging.support.MessageBuilder;

import com.azure.spring.messaging.servicebus.core.ServiceBusTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherImpl implements EventPublisher {
    
    private final ServiceBusTemplate serviceBusTemplate;
    
    @Override
    public void publish(String eventName, Object event) {
        try {
            Message<Object> message = MessageBuilder
                .withPayload(event)
                .setHeader("event-name", eventName)
                .build();
            
            serviceBusTemplate.send(eventName, message);
            log.info("Published event: {} to topic: {}", eventName, eventName);
        } catch (Exception e) {
            log.error("Failed to publish event: {}", eventName, e);
            throw new RuntimeException("Failed to publish event", e);
        }
    }
}
