package com.youtube.mvp.feeds.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.mvp.feeds.application.dto.FeedDto;
import com.youtube.mvp.feeds.application.dto.FeedItemDto;
import com.youtube.mvp.feeds.application.usecase.GetFeedUseCase;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FeedController.class)
class FeedControllerTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @MockBean
    private GetFeedUseCase getFeedUseCase;
    
    @Autowired
    private ObjectMapper objectMapper;
    
    @Test
    @WithMockUser
    void testGetHomeFeed() throws Exception {
        // Given
        String userId = "user-123";
        FeedDto mockFeed = createMockFeed();
        
        when(getFeedUseCase.execute(eq(userId), any(), anyInt(), isNull()))
                .thenReturn(mockFeed);
        
        // When/Then
        mockMvc.perform(get("/api/v1/feeds/home")
                        .with(jwt().jwt(jwt -> jwt.subject(userId)))
                        .param("pageSize", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(header().exists("ETag"))
                .andExpect(jsonPath("$.items").isArray())
                .andExpect(jsonPath("$.total_count").value(50));
    }
    
    @Test
    @WithMockUser
    void testGetHomeFeed_WithIfNoneMatch() throws Exception {
        // Given
        String userId = "user-123";
        String etag = "etag-123";
        FeedDto mockFeed = createMockFeed();
        
        when(getFeedUseCase.execute(eq(userId), any(), anyInt(), isNull()))
                .thenReturn(mockFeed);
        
        // When/Then
        mockMvc.perform(get("/api/v1/feeds/home")
                        .with(jwt().jwt(jwt -> jwt.subject(userId)))
                        .header("If-None-Match", etag)
                        .param("pageSize", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk()); // If ETag matches, should return 304
    }
    
    @Test
    @WithMockUser
    void testGetSubscriptionsFeed() throws Exception {
        // Given
        String userId = "user-123";
        FeedDto mockFeed = createMockFeed();
        
        when(getFeedUseCase.execute(eq(userId), any(), anyInt(), isNull()))
                .thenReturn(mockFeed);
        
        // When/Then
        mockMvc.perform(get("/api/v1/feeds/subscriptions")
                        .with(jwt().jwt(jwt -> jwt.subject(userId)))
                        .param("pageSize", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }
    
    @Test
    @WithMockUser
    void testGetTrendingFeed() throws Exception {
        // Given
        String userId = "user-123";
        FeedDto mockFeed = createMockFeed();
        
        when(getFeedUseCase.execute(eq(userId), any(), anyInt(), isNull()))
                .thenReturn(mockFeed);
        
        // When/Then
        mockMvc.perform(get("/api/v1/feeds/trending")
                        .with(jwt().jwt(jwt -> jwt.subject(userId)))
                        .param("pageSize", "50")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.items").isArray());
    }
    
    @Test
    @WithMockUser
    void testGetHomeFeed_InvalidPageSize() throws Exception {
        // Given
        String userId = "user-123";
        
        // When/Then
        mockMvc.perform(get("/api/v1/feeds/home")
                        .with(jwt().jwt(jwt -> jwt.subject(userId)))
                        .param("pageSize", "200") // exceeds max
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());
    }
    
    private FeedDto createMockFeed() {
        List<FeedItemDto> items = java.util.stream.IntStream.range(0, 50)
                .mapToObj(i -> FeedItemDto.builder()
                        .videoId("video-" + i)
                        .title("Video " + i)
                        .channelId("channel-" + (i % 10))
                        .channelName("Channel " + (i % 10))
                        .thumbnailUrl("https://via.placeholder.com/640x480")
                        .publishedAt(Instant.now().minusSeconds(i * 3600))
                        .viewCount((long) (1000 + i * 100))
                        .durationSeconds((long) (60 + i))
                        .isAd(false)
                        .adSlotIndex(null)
                        .category("entertainment")
                        .build())
                .toList();
        
        return FeedDto.builder()
                .items(items)
                .lastUpdated(Instant.now())
                .etag("etag-123")
                .totalCount(50L)
                .pageSize(50)
                .nextPageToken(null)
                .build();
    }
}

