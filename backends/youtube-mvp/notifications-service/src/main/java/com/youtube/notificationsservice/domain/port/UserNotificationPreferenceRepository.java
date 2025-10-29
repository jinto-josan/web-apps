package com.youtube.notificationsservice.domain.port;

import com.youtube.notificationsservice.domain.model.NotificationPreference;

import java.util.Optional;

public interface UserNotificationPreferenceRepository {
    Optional<NotificationPreference> findByUserId(String tenantId, String userId);
    NotificationPreference save(NotificationPreference preference);
}


