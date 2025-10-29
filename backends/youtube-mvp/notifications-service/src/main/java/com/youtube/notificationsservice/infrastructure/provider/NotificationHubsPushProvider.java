package com.youtube.notificationsservice.infrastructure.provider;

import com.youtube.notificationsservice.application.port.NotificationProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class NotificationHubsPushProvider implements NotificationProviderPort {
    private static final Logger log = LoggerFactory.getLogger(NotificationHubsPushProvider.class);

    @Override
    public void sendEmail(String toEmail, String subject, String content) {
        // no-op in push provider
    }

    @Override
    public void sendPush(String deviceToken, String title, String body) {
        // Stub: integrate Azure Notification Hubs SDK here
        log.info("Sending push to token {} title {}", deviceToken, title);
    }
}


