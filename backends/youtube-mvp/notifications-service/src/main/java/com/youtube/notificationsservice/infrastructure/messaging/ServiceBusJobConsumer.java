package com.youtube.notificationsservice.infrastructure.messaging;

import com.azure.spring.messaging.servicebus.annotation.ServiceBusListener;
import com.youtube.notificationsservice.application.port.NotificationProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class ServiceBusJobConsumer {

    private static final Logger log = LoggerFactory.getLogger(ServiceBusJobConsumer.class);

    private final NotificationProviderPort emailProvider;
    private final NotificationProviderPort pushProvider;
    private final StringRedisTemplate redis;

    public ServiceBusJobConsumer(NotificationProviderPort emailProvider,
                                 NotificationProviderPort pushProvider,
                                 StringRedisTemplate redis) {
        this.emailProvider = emailProvider;
        this.pushProvider = pushProvider;
        this.redis = redis;
    }

    @ServiceBusListener(destination = "${app.servicebus.topics.notifications}", subscription = "worker")
    public void onMessage(String payload) {
        // Very simple idempotency using payload hash
        String key = "idem:" + Integer.toHexString(payload.hashCode());
        Boolean success = redis.opsForValue().setIfAbsent(key, "1", Duration.ofMinutes(10));
        if (Boolean.FALSE.equals(success)) {
            log.info("Duplicate job ignored");
            return;
        }
        log.info("Processing job: {}", payload);
        // Demo: call providers based on channel field presence (omitted parser for brevity)
        emailProvider.sendEmail("user@example.com", "Test", payload);
    }
}


