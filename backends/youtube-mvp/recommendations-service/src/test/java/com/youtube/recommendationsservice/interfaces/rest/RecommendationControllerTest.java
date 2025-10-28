package com.youtube.recommendationsservice.interfaces.rest;

import com.youtube.recommendationsservice.application.dto.RecommendationResponse;
import com.youtube.recommendationsservice.application.usecases.GetHomeRecommendationsUseCase;
import com.youtube.recommendationsservice.application.usecases.GetNextUpRecommendationsUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(RecommendationController.class)
class RecommendationControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private GetHomeRecommendationsUseCase getHomeRecommendationsUseCase;
    
    @MockBean
    private GetNextUpRecommendationsUseCase getNextUpRecommendationsUseCase;
    
    @Test
    @WithMockUser
    void getHomeRecommendations_ShouldReturnOk() throws Exception {
        // Given
        RecommendationResponse response = RecommendationResponse.builder()
            .recommendations(List.of())
            .metadata(RecommendationResponse.Metadata.builder()
                .userId("user123")
                .requestType("home")
                .timestamp(java.time.Instant.now())
                .totalCandidates(10)
                .totalReturned(5)
                .build())
            .build();
        
        when(getHomeRecommendationsUseCase.execute(any())).thenReturn(response);
        
        // When & Then
        mockMvc.perform(get("/api/v1/recs/home")
                .param("userId", "user123")
                .param("limit", "10")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.metadata.userId").value("user123"));
    }
    
    @Test
    @WithMockUser
    void getNextUpRecommendations_ShouldReturnOk() throws Exception {
        // Given
        RecommendationResponse response = RecommendationResponse.builder()
            .recommendations(List.of())
            .metadata(RecommendationResponse.Metadata.builder()
                .userId("user123")
                .requestType("next-up")
                .timestamp(java.time.Instant.now())
                .totalCandidates(10)
                .totalReturned(5)
                .build())
            .build();
        
        when(getNextUpRecommendationsUseCase.execute(any())).thenReturn(response);
        
        // When & Then
        mockMvc.perform(get("/api/v1/recs/next")
                .param("userId", "user123")
                .param("videoId", "video123")
                .param("limit", "10")
                .accept(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.APPLICATION_JSON))
            .andExpect(jsonPath("$.metadata.userId").value("user123"));
    }
}

