package com.youtube.livestreaming.infrastructure.adapters.messaging;

import com.youtube.livestreaming.domain.ports.EventPublisher;
import com.youtube.livestreaming.infrastructure.config.ServiceBusConfig;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.azure.servicebus.ServiceBusTemplate;
import org.springframework.stereotype.Component;

/**
 * Service Bus event publisher adapter
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceBusEventPublisher implements EventPublisher {
    
    private final ServiceBusConfig serviceBusConfig;
    // In a real implementation, inject ServiceBusProducerFactory
    // For now, this is a stub
    
    @Override
    public void publish(Object event) {
        log.info("Publishing domain event: {}", event.getClass().getSimpleName());
        
        // TODO: Implement actual Service Bus publishing
        // var producer = serviceBusConfig.getProducerFactory().createProducer("live-events");
        // producer.sendMessage(new ServiceBusMessage(serialize(event)));
    }
    
    @Override
    public void publishAll(Iterable<Object> events) {
        events.forEach(this::publish);
    }
}

