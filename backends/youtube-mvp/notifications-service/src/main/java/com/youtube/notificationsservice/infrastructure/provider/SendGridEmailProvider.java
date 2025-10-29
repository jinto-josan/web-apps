package com.youtube.notificationsservice.infrastructure.provider;

import com.youtube.notificationsservice.application.port.NotificationProviderPort;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class SendGridEmailProvider implements NotificationProviderPort {
    private static final Logger log = LoggerFactory.getLogger(SendGridEmailProvider.class);

    @Override
    public void sendEmail(String toEmail, String subject, String content) {
        // Stub: integrate SendGrid client here
        log.info("Sending email to {} subject {}", toEmail, subject);
    }

    @Override
    public void sendPush(String deviceToken, String title, String body) {
        // no-op in email provider
    }
}


