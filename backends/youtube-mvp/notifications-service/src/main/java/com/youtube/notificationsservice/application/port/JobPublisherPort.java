package com.youtube.notificationsservice.application.port;

public interface JobPublisherPort {
    void publishTestNotificationJob(String tenantId, String userId, String channel);
}


