package com.youtube.mvp.feeds.application.usecase;

import com.youtube.mvp.feeds.application.dto.FeedDto;
import com.youtube.mvp.feeds.domain.model.Feed;
import com.youtube.mvp.feeds.domain.model.FeedType;
import com.youtube.mvp.feeds.domain.repository.FeedRepository;
import com.youtube.mvp.feeds.domain.repository.SubscriptionRepository;
import com.youtube.mvp.feeds.domain.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class GetFeedUseCase {
    
    private final FeedRepository feedRepository;
    private final VideoRepository videoRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final FeedCacheService feedCacheService;
    private final AdSlotService adSlotService;
    
    @Transactional(readOnly = true)
    @Cacheable(value = "feeds", key = "#userId + ':' + #feedType", unless = "#result == null")
    public FeedDto execute(String userId, FeedType feedType, Integer pageSize, String pageToken) {
        log.info("Getting feed for userId={}, feedType={}", userId, feedType);
        
        // Try to get from cache first
        Feed feed = feedRepository.findByUserIdAndFeedType(userId, feedType)
                .orElseGet(() -> {
                    log.info("Cache miss for userId={}, feedType={}. Generating feed.", userId, feedType);
                    return generateFeed(userId, feedType, pageSize);
                });
        
        // Inject ads if needed
        if (feed.getItems() != null && !feed.getItems().isEmpty()) {
            List<com.youtube.mvp.feeds.domain.model.FeedItem> itemsWithAds = 
                    adSlotService.injectAds(feed.getItems(), feedType);
            
            feed = Feed.builder()
                    .userId(feed.getUserId())
                    .feedType(feed.getFeedType())
                    .items(itemsWithAds)
                    .lastUpdated(Instant.now())
                    .etag(UUID.randomUUID().toString())
                    .totalCount((long) itemsWithAds.size())
                    .pageSize(feed.getPageSize())
                    .nextPageToken(feed.getNextPageToken())
                    .build();
        }
        
        return mapToDto(feed);
    }
    
    private Feed generateFeed(String userId, FeedType feedType, Integer pageSize) {
        List<com.youtube.mvp.feeds.domain.model.FeedItem> items;
        
        switch (feedType) {
            case HOME -> {
                items = videoRepository.findRecommendedForUser(userId, pageSize != null ? pageSize : 50);
            }
            case SUBSCRIPTIONS -> {
                List<String> subscribedChannels = subscriptionRepository.findSubscribedChannels(userId);
                items = subscribedChannels.isEmpty() 
                        ? List.of() 
                        : videoRepository.findByChannelIds(subscribedChannels, pageSize != null ? pageSize : 50);
            }
            case TRENDING -> {
                items = videoRepository.findTrending(pageSize != null ? pageSize : 50, null);
            }
            default -> items = List.of();
        }
        
        return Feed.builder()
                .userId(userId)
                .feedType(feedType)
                .items(items)
                .lastUpdated(Instant.now())
                .etag(UUID.randomUUID().toString())
                .totalCount((long) items.size())
                .pageSize(pageSize)
                .nextPageToken(null)
                .build();
    }
    
    private FeedDto mapToDto(Feed feed) {
        return FeedDto.builder()
                .items(feed.getItems().stream().map(this::mapToDto).toList())
                .lastUpdated(feed.getLastUpdated())
                .etag(feed.getEtag())
                .totalCount(feed.getTotalCount())
                .pageSize(feed.getPageSize())
                .nextPageToken(feed.getNextPageToken())
                .build();
    }
    
    private com.youtube.mvp.feeds.application.dto.FeedItemDto mapToDto(com.youtube.mvp.feeds.domain.model.FeedItem item) {
        return com.youtube.mvp.feeds.application.dto.FeedItemDto.builder()
                .videoId(item.getVideoId())
                .title(item.getTitle())
                .channelId(item.getChannelId())
                .channelName(item.getChannelName())
                .thumbnailUrl(item.getThumbnailUrl())
                .publishedAt(item.getPublishedAt())
                .viewCount(item.getViewCount())
                .durationSeconds(item.getDurationSeconds())
                .isAd(item.isAd())
                .adSlotIndex(item.getAdSlotIndex())
                .category(item.getCategory())
                .build();
    }
}

