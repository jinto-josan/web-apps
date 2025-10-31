package com.youtube.configsecretsservice.infrastructure.messaging;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.configsecretsservice.domain.port.EventPublisherPort;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Adapter implementing EventPublisherPort using Azure Service Bus.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ServiceBusEventPublisherAdapter implements EventPublisherPort {
    
    private final ServiceBusSenderClient serviceBusSenderClient;
    private final ObjectMapper objectMapper;
    
    @Value("${azure.servicebus.config-update-topic:config-updates}")
    private String configUpdateTopic;
    
    @Override
    public void publishConfigurationUpdated(String scope, String key, String etag) {
        try {
            ConfigUpdateEvent event = ConfigUpdateEvent.builder()
                    .scope(scope)
                    .key(key)
                    .etag(etag)
                    .timestamp(java.time.Instant.now())
                    .build();
            
            String messageBody = objectMapper.writeValueAsString(event);
            ServiceBusMessage message = new ServiceBusMessage(messageBody);
            message.setSubject("ConfigurationUpdated");
            message.getApplicationProperties().put("scope", scope);
            message.getApplicationProperties().put("key", key);
            
            serviceBusSenderClient.sendMessage(message);
            log.info("Published configuration updated event: {}/{}", scope, key);
        } catch (JsonProcessingException e) {
            log.error("Error serializing configuration update event: {}/{}", scope, key, e);
        } catch (Exception e) {
            log.error("Error publishing configuration update event: {}/{}", scope, key, e);
        }
    }
    
    @Override
    public void publishSecretRotationCompleted(String scope, String key, boolean success) {
        try {
            SecretRotationEvent event = SecretRotationEvent.builder()
                    .scope(scope)
                    .key(key)
                    .success(success)
                    .timestamp(java.time.Instant.now())
                    .build();
            
            String messageBody = objectMapper.writeValueAsString(event);
            ServiceBusMessage message = new ServiceBusMessage(messageBody);
            message.setSubject("SecretRotationCompleted");
            message.getApplicationProperties().put("scope", scope);
            message.getApplicationProperties().put("key", key);
            message.getApplicationProperties().put("success", success);
            
            serviceBusSenderClient.sendMessage(message);
            log.info("Published secret rotation completed event: {}/{}", scope, key);
        } catch (JsonProcessingException e) {
            log.error("Error serializing secret rotation event: {}/{}", scope, key, e);
        } catch (Exception e) {
            log.error("Error publishing secret rotation event: {}/{}", scope, key, e);
        }
    }
    
    @lombok.Value
    @lombok.Builder
    static class ConfigUpdateEvent {
        String scope;
        String key;
        String etag;
        java.time.Instant timestamp;
    }
    
    @lombok.Value
    @lombok.Builder
    static class SecretRotationEvent {
        String scope;
        String key;
        boolean success;
        java.time.Instant timestamp;
    }
}

