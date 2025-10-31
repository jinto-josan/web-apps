package com.youtube.edgecdncontrol.interfaces.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.edgecdncontrol.application.dto.CreateCdnRuleRequest;
import com.youtube.edgecdncontrol.application.dto.CdnRuleResponse;
import com.youtube.edgecdncontrol.application.usecases.CreateCdnRuleUseCase;
import com.youtube.edgecdncontrol.domain.valueobjects.RuleStatus;
import com.youtube.edgecdncontrol.domain.valueobjects.RuleType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CdnRuleController.class)
class CdnRuleControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private CreateCdnRuleUseCase createCdnRuleUseCase;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @WithMockUser
    void shouldCreateRule() throws Exception {
        // Given
        CreateCdnRuleRequest request = new CreateCdnRuleRequest();
        request.setName("Test Rule");
        request.setRuleType(RuleType.ROUTING_RULE);
        request.setResourceGroup("rg-test");
        request.setFrontDoorProfileName("fd-test");
        
        CdnRuleResponse response = CdnRuleResponse.builder()
                .id("test-id")
                .name("Test Rule")
                .status(RuleStatus.DRAFT)
                .version("v1")
                .build();
        
        when(createCdnRuleUseCase.execute(any(), eq("user"))).thenReturn(response);
        
        // When/Then
        mockMvc.perform(post("/api/v1/cdn/rules")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(jwt()))
                .andExpect(status().isCreated())
                .andExpect(header().exists("ETag"));
    }
}

