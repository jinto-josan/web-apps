package com.youtube.mvp.feeds.application.usecase;

import com.youtube.mvp.feeds.domain.model.Feed;
import com.youtube.mvp.feeds.domain.model.FeedItem;
import com.youtube.mvp.feeds.domain.model.FeedType;
import com.youtube.mvp.feeds.domain.repository.FeedRepository;
import com.youtube.mvp.feeds.domain.repository.SubscriptionRepository;
import com.youtube.mvp.feeds.domain.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class GetFeedUseCaseTest {
    
    @Mock
    private FeedRepository feedRepository;
    
    @Mock
    private VideoRepository videoRepository;
    
    @Mock
    private SubscriptionRepository subscriptionRepository;
    
    @Mock
    private FeedCacheService feedCacheService;
    
    @Mock
    private AdSlotService adSlotService;
    
    @InjectMocks
    private GetFeedUseCase getFeedUseCase;
    
    private String userId;
    
    @BeforeEach
    void setUp() {
        userId = "user-123";
    }
    
    @Test
    void testGetHomeFeed_CacheHit() {
        // Given
        Feed cachedFeed = Feed.builder()
                .userId(userId)
                .feedType(FeedType.HOME)
                .items(createMockFeedItems(5))
                .lastUpdated(Instant.now())
                .etag("etag-123")
                .totalCount(5L)
                .pageSize(50)
                .nextPageToken(null)
                .build();
        
        when(feedRepository.findByUserIdAndFeedType(userId, FeedType.HOME))
                .thenReturn(Optional.of(cachedFeed));
        
        when(adSlotService.injectAds(anyList(), eq(FeedType.HOME)))
                .thenReturn(cachedFeed.getItems());
        
        // When
        var result = getFeedUseCase.execute(userId, FeedType.HOME, 50, null);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(5);
        assertThat(result.getTotalCount()).isEqualTo(5L);
        
        verify(feedRepository).findByUserIdAndFeedType(userId, FeedType.HOME);
        verify(adSlotService).injectAds(anyList(), eq(FeedType.HOME));
        verifyNoMoreInteractions(videoRepository);
    }
    
    @Test
    void testGetHomeFeed_CacheMiss() {
        // Given
        when(feedRepository.findByUserIdAndFeedType(userId, FeedType.HOME))
                .thenReturn(Optional.empty());
        
        List<FeedItem> recommendedVideos = createMockFeedItems(50);
        when(videoRepository.findRecommendedForUser(userId, 50))
                .thenReturn(recommendedVideos);
        
        when(adSlotService.injectAds(anyList(), eq(FeedType.HOME)))
                .thenReturn(recommendedVideos);
        
        // When
        var result = getFeedUseCase.execute(userId, FeedType.HOME, 50, null);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).hasSize(50);
        
        verify(videoRepository).findRecommendedForUser(userId, 50);
        verify(adSlotService).injectAds(anyList(), eq(FeedType.HOME));
    }
    
    @Test
    void testGetSubscriptionsFeed() {
        // Given
        when(feedRepository.findByUserIdAndFeedType(userId, FeedType.SUBSCRIPTIONS))
                .thenReturn(Optional.empty());
        
        when(subscriptionRepository.findSubscribedChannels(userId))
                .thenReturn(List.of("channel-1", "channel-2"));
        
        List<FeedItem> subscriptionVideos = createMockFeedItems(30);
        when(videoRepository.findByChannelIds(anyList(), eq(50)))
                .thenReturn(subscriptionVideos);
        
        when(adSlotService.injectAds(anyList(), eq(FeedType.SUBSCRIPTIONS)))
                .thenReturn(subscriptionVideos);
        
        // When
        var result = getFeedUseCase.execute(userId, FeedType.SUBSCRIPTIONS, 50, null);
        
        // Then
        assertThat(result).isNotNull();
        
        verify(subscriptionRepository).findSubscribedChannels(userId);
        verify(videoRepository).findByChannelIds(anyList(), eq(50));
    }
    
    @Test
    void testGetSubscriptionsFeed_NoSubscriptions() {
        // Given
        when(feedRepository.findByUserIdAndFeedType(userId, FeedType.SUBSCRIPTIONS))
                .thenReturn(Optional.empty());
        
        when(subscriptionRepository.findSubscribedChannels(userId))
                .thenReturn(List.of());
        
        // When
        var result = getFeedUseCase.execute(userId, FeedType.SUBSCRIPTIONS, 50, null);
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getItems()).isEmpty();
        
        verify(subscriptionRepository).findSubscribedChannels(userId);
        verifyNoInteractions(videoRepository);
    }
    
    @Test
    void testGetTrendingFeed() {
        // Given
        when(feedRepository.findByUserIdAndFeedType(userId, FeedType.TRENDING))
                .thenReturn(Optional.empty());
        
        List<FeedItem> trendingVideos = createMockFeedItems(50);
        when(videoRepository.findTrending(50, null))
                .thenReturn(trendingVideos);
        
        when(adSlotService.injectAds(anyList(), eq(FeedType.TRENDING)))
                .thenReturn(trendingVideos);
        
        // When
        var result = getFeedUseCase.execute(userId, FeedType.TRENDING, 50, null);
        
        // Then
        assertThat(result).isNotNull();
        
        verify(videoRepository).findTrending(50, null);
        verify(adSlotService).injectAds(anyList(), eq(FeedType.TRENDING));
    }
    
    private List<FeedItem> createMockFeedItems(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(i -> FeedItem.builder()
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
    }
}

