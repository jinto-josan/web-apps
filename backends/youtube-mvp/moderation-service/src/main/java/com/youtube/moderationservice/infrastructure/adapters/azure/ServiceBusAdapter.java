package com.youtube.moderationservice.infrastructure.adapters.azure;

import com.azure.spring.messaging.servicebus.core.ServiceBusTemplate;
import com.youtube.moderationservice.application.ports.ServiceBusPort;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ServiceBusAdapter implements ServiceBusPort {

    private final ServiceBusTemplate serviceBusTemplate;

    @Value("${moderation.review.topic:moderation-review}")
    private String reviewTopic;

    @Override
    public void publishReviewTask(String caseId, String payload) {
        serviceBusTemplate.convertAndSend(reviewTopic, payload);
    }
}


