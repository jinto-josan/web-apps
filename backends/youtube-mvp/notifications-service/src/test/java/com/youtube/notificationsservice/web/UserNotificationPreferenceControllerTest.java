package com.youtube.notificationsservice.web;

import com.youtube.notificationsservice.application.dto.NotificationPreferenceDto;
import com.youtube.notificationsservice.application.service.NotificationPreferenceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = UserNotificationPreferenceController.class)
class UserNotificationPreferenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationPreferenceService service;

    @Test
    void getShouldReturnOk() throws Exception {
        NotificationPreferenceDto dto = new NotificationPreferenceDto();
        dto.setUserId("user-1");
        dto.setTenantId("t1");
        dto.setEmailEnabled(true);
        dto.setPushEnabled(false);
        dto.setInAppEnabled(true);

        when(service.get("t1", "user-1")).thenReturn(Optional.of(dto));

        mockMvc.perform(get("/api/v1/users/user-1/notification-prefs")
                .param("tenantId", "t1")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk());
    }
}


