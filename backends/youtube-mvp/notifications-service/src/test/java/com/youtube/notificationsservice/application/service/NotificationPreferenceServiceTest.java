package com.youtube.notificationsservice.application.service;

import com.youtube.notificationsservice.application.dto.NotificationPreferenceDto;
import com.youtube.notificationsservice.application.mapper.NotificationPreferenceMapper;
import com.youtube.notificationsservice.domain.model.NotificationPreference;
import com.youtube.notificationsservice.domain.port.UserNotificationPreferenceRepository;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class NotificationPreferenceServiceTest {

    private final UserNotificationPreferenceRepository repository = mock(UserNotificationPreferenceRepository.class);
    private final NotificationPreferenceMapper mapper = Mappers.getMapper(NotificationPreferenceMapper.class);
    private final NotificationPreferenceService service = new NotificationPreferenceService(repository, mapper);

    @Test
    void getReturnsDtoWhenFound() {
        NotificationPreference pref = NotificationPreference.builder()
                .id("user-1").tenantId("t1").emailEnabled(true).pushEnabled(false).inAppEnabled(true).build();
        when(repository.findByUserId("t1", "user-1")).thenReturn(Optional.of(pref));

        Optional<NotificationPreferenceDto> result = service.get("t1", "user-1");
        assertThat(result).isPresent();
        assertThat(result.get().getUserId()).isEqualTo("user-1");
    }
}


