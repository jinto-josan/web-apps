package com.youtube.commentsservice.infrastructure.messaging;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class ServiceBusTopicSender {
    
    private final ServiceBusSenderClient senderClient;
    
    public void send(String topicName, String messageBody, Map<String, String> properties) {
        ServiceBusMessage message = new ServiceBusMessage(messageBody);
        
        if (properties != null) {
            properties.forEach(message::setMessageId);
            properties.forEach((key, value) -> message.getApplicationProperties().put(key, value));
        }
        
        senderClient.sendMessage(message);
        log.debug("Sent message to topic: {}", topicName);
    }
}

