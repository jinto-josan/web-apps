package com.youtube.notificationsservice.application.mapper;

import com.youtube.notificationsservice.application.dto.NotificationPreferenceDto;
import com.youtube.notificationsservice.domain.model.NotificationPreference;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface NotificationPreferenceMapper {

    @Mapping(target = "id", source = "userId")
    @Mapping(target = "etag", ignore = true)
    NotificationPreference toDomain(NotificationPreferenceDto dto);

    @Mapping(target = "userId", source = "id")
    NotificationPreferenceDto toDto(NotificationPreference preference);
}


