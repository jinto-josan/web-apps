package com.youtube.mvp.search.presentation.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.mvp.search.application.dto.SearchRequest;
import com.youtube.mvp.search.application.dto.SearchResponse;
import com.youtube.mvp.search.application.dto.SuggestionRequest;
import com.youtube.mvp.search.application.dto.SuggestionResponse;
import com.youtube.mvp.search.application.service.SearchApplicationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(SearchController.class)
class SearchControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private SearchApplicationService searchApplicationService;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @WithMockUser
    void search_ShouldReturnResults() throws Exception {
        // Given
        SearchResponse response = SearchResponse.builder()
                .totalCount(100L)
                .page(1)
                .pageSize(20)
                .hasMore(true)
                .build();
        
        when(searchApplicationService.search(any(SearchRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(get("/api/v1/search")
                        .param("query", "java tutorial")
                        .param("page", "1")
                        .param("pageSize", "20")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalCount").value(100L))
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.hasMore").value(true))
                .andExpect(header().exists("ETag"));
    }
    
    @Test
    @WithMockUser
    void search_InvalidRequest_ShouldReturn400() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                        .param("query", "") // Empty query
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    
    @Test
    @WithMockUser
    void suggest_ShouldReturnSuggestions() throws Exception {
        // Given
        SuggestionResponse response = SuggestionResponse.builder()
                .suggestions(Arrays.asList(
                        SuggestionResponse.SuggestionItem.builder()
                                .text("java tutorial")
                                .score(100)
                                .build()
                ))
                .build();
        
        when(searchApplicationService.suggest(any(SuggestionRequest.class))).thenReturn(response);
        
        // When & Then
        mockMvc.perform(get("/api/v1/suggest")
                        .param("prefix", "java")
                        .param("maxResults", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.suggestions").isArray())
                .andExpect(jsonPath("$.suggestions[0].text").value("java tutorial"));
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void rebuildIndex_Admin_ShouldReturn202() throws Exception {
        mockMvc.perform(post("/api/v1/index/rebuild")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"forceRebuild\":true}"))
                .andExpect(status().isAccepted());
    }
    
    @Test
    @WithMockUser(roles = "USER")
    void rebuildIndex_NonAdmin_ShouldReturn403() throws Exception {
        mockMvc.perform(post("/api/v1/index/rebuild")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isForbidden());
    }
    
    @Test
    void search_Unauthenticated_ShouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/search")
                        .param("query", "test"))
                .andExpect(status().isUnauthorized());
    }
}
