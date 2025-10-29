package com.youtube.notificationsservice.infrastructure.messaging;

import com.azure.spring.messaging.servicebus.core.ServiceBusTemplate;
import com.youtube.notificationsservice.application.port.JobPublisherPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;

@Component
public class ServiceBusJobPublisher implements JobPublisherPort {

    private final ServiceBusTemplate serviceBusTemplate;
    private final String topicName;

    public ServiceBusJobPublisher(ServiceBusTemplate serviceBusTemplate,
                                  @Value("${app.servicebus.topics.notifications}") String topicName) {
        this.serviceBusTemplate = serviceBusTemplate;
        this.topicName = topicName;
    }

    @Override
    public void publishTestNotificationJob(String tenantId, String userId, String channel) {
        var payload = String.format("{\"type\":\"TEST\",\"tenantId\":\"%s\",\"userId\":\"%s\",\"channel\":\"%s\"}", tenantId, userId, channel);
        serviceBusTemplate.send(topicName, MessageBuilder.withPayload(payload).build());
    }
}


