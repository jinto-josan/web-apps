package com.youtube.notificationsservice.infrastructure.repository;

import com.azure.spring.data.cosmos.repository.CosmosRepository;
import com.youtube.notificationsservice.domain.model.NotificationPreference;
import com.youtube.notificationsservice.domain.port.UserNotificationPreferenceRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

interface SpringCosmosPreferenceRepo extends CosmosRepository<NotificationPreference, String> {
}

@Repository
public class CosmosUserNotificationPreferenceRepository implements UserNotificationPreferenceRepository {

    private final SpringCosmosPreferenceRepo repo;

    public CosmosUserNotificationPreferenceRepository(SpringCosmosPreferenceRepo repo) {
        this.repo = repo;
    }

    @Override
    public Optional<NotificationPreference> findByUserId(String tenantId, String userId) {
        return repo.findById(userId);
    }

    @Override
    public NotificationPreference save(NotificationPreference preference) {
        return repo.save(preference);
    }
}


