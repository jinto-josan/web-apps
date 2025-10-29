package com.youtube.notificationsservice.application.port;

public interface NotificationProviderPort {
    void sendEmail(String toEmail, String subject, String content);
    void sendPush(String deviceToken, String title, String body);
}


