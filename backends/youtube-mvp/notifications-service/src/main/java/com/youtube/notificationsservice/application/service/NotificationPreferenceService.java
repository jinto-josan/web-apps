package com.youtube.notificationsservice.application.service;

import com.youtube.notificationsservice.application.dto.NotificationPreferenceDto;
import com.youtube.notificationsservice.application.mapper.NotificationPreferenceMapper;
import com.youtube.notificationsservice.domain.model.NotificationPreference;
import com.youtube.notificationsservice.domain.port.UserNotificationPreferenceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class NotificationPreferenceService {

    private final UserNotificationPreferenceRepository repository;
    private final NotificationPreferenceMapper mapper;

    public NotificationPreferenceService(UserNotificationPreferenceRepository repository, NotificationPreferenceMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public Optional<NotificationPreferenceDto> get(String tenantId, String userId) {
        return repository.findByUserId(tenantId, userId).map(mapper::toDto);
    }

    @Transactional
    public NotificationPreferenceDto upsert(NotificationPreferenceDto dto) {
        NotificationPreference toSave = mapper.toDomain(dto);
        NotificationPreference saved = repository.save(toSave);
        return mapper.toDto(saved);
    }
}


