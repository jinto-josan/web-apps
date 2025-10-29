package com.youtube.livechatservice.infrastructure.messaging;

import com.azure.spring.messaging.servicebus.core.ServiceBusTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceBusModerationProducer {

    private final ServiceBusTemplate serviceBusTemplate;

    @Value("${livechat.moderation.topic:moderation-events}")
    private String topicName;

    public void publish(ModerationEvent event) {
        serviceBusTemplate.sendAsync(topicName, MessageBuilder.withPayload(event).build());
    }
}


