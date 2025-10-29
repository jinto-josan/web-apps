package com.youtube.moderationservice.infrastructure.adapters.azure;

import com.azure.spring.messaging.servicebus.annotation.ServiceBusListener;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ReviewTaskListener {

    @ServiceBusListener(topics = "${moderation.review.topic:moderation-review}", subscription = "${moderation.review.subscription:default}")
    public void onMessage(@Payload String payload) {
        log.info("Received review task: {}", payload);
        // TODO: update case state/idempotent consumer via Redis if needed
    }
}


