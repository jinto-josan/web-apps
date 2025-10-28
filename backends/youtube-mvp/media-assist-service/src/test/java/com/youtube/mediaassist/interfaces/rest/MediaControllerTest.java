package com.youtube.mediaassist.interfaces.rest;

import com.youtube.mediaassist.application.usecases.MediaAccessUseCase;
import com.youtube.mediaassist.infrastructure.external.AzureBlobStorageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MediaController.class)
public class MediaControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private MediaAccessUseCase mediaAccessUseCase;
    
    @MockBean
    private AzureBlobStorageService blobStorageService;
    
    @Test
    @WithMockUser
    public void testGenerateSasUrl() throws Exception {
        // TODO: Implement test
    }
}

