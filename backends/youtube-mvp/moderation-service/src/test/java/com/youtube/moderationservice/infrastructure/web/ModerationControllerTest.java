package com.youtube.moderationservice.infrastructure.web;

import com.youtube.moderationservice.application.service.ModerationApplicationService;
import com.youtube.moderationservice.infrastructure.web.controller.ModerationController;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = ModerationController.class)
class ModerationControllerTest {
    @Autowired MockMvc mockMvc;
    @MockBean ModerationApplicationService service;

    @Test
    void scan_returnsOk() throws Exception {
        when(service.scanContent(any(), anyMap())).thenReturn(Map.of("HATE_SPEECH", 0.1));
        mockMvc.perform(post("/api/v1/moderation/scan")
                .contentType("application/json")
                .content("{\"content\":\"hello\",\"contentType\":\"text\"}"))
            .andExpect(status().isOk());
    }
}


