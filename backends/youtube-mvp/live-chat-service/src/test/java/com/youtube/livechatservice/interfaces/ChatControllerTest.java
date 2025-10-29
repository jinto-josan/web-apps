package com.youtube.livechatservice.interfaces;

import com.youtube.livechatservice.infrastructure.external.WebPubSubAdapter;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest
class ChatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private WebPubSubAdapter webPubSubAdapter;

    @Test
    void issueToken_returnsOk() throws Exception {
        Mockito.when(webPubSubAdapter.issueClientToken(Mockito.anyString(), Mockito.any()))
                .thenReturn(new WebPubSubAdapter.Token("t", java.time.Instant.now().plusSeconds(3600)));

        mockMvc.perform(post("/api/v1/live/abc/chat/token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"userId\":\"u1\",\"role\":\"viewer\"}"))
                .andExpect(status().isOk());
    }
}


