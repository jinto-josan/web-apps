package com.youtube.drmservice.infrastructure.services;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.drmservice.domain.services.EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventPublisherImpl implements EventPublisher {

    private final ServiceBusSenderClient serviceBusSenderClient;
    private final ObjectMapper objectMapper;

    @Override
    public void publishPolicyCreated(Object event) {
        try {
            String messageBody = objectMapper.writeValueAsString(event);
            ServiceBusMessage message = new ServiceBusMessage(messageBody)
                    .setSubject("drm.policy.created");
            
            serviceBusSenderClient.sendMessage(message);
            log.info("Published policy created event: {}", event);
        } catch (Exception e) {
            log.error("Error publishing policy created event", e);
        }
    }

    @Override
    public void publishPolicyUpdated(Object event) {
        try {
            String messageBody = objectMapper.writeValueAsString(event);
            ServiceBusMessage message = new ServiceBusMessage(messageBody)
                    .setSubject("drm.policy.updated");
            
            serviceBusSenderClient.sendMessage(message);
            log.info("Published policy updated event: {}", event);
        } catch (Exception e) {
            log.error("Error publishing policy updated event", e);
        }
    }

    @Override
    public void publishKeyRotationTriggered(Object event) {
        try {
            String messageBody = objectMapper.writeValueAsString(event);
            ServiceBusMessage message = new ServiceBusMessage(messageBody)
                    .setSubject("drm.key.rotation.triggered");
            
            serviceBusSenderClient.sendMessage(message);
            log.info("Published key rotation event: {}", event);
        } catch (Exception e) {
            log.error("Error publishing key rotation event", e);
        }
    }
}

