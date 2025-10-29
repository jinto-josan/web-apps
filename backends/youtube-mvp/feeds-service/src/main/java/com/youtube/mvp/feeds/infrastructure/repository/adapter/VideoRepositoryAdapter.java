package com.youtube.mvp.feeds.infrastructure.repository.adapter;

import com.youtube.mvp.feeds.domain.model.FeedItem;
import com.youtube.mvp.feeds.domain.repository.VideoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Repository
@RequiredArgsConstructor
public class VideoRepositoryAdapter implements VideoRepository {
    
    private final Random random = new Random();
    
    @Override
    public List<FeedItem> findRecentPublished(int limit) {
        // In production, this would query video-catalog-service
        log.info("Finding recent published videos, limit={}", limit);
        return generateMockVideos(limit);
    }
    
    @Override
    public List<FeedItem> findRecommendedForUser(String userId, int limit) {
        // In production, this would call recommendations-service
        log.info("Finding recommended videos for userId={}, limit={}", userId, limit);
        return generateMockVideos(limit);
    }
    
    @Override
    public List<FeedItem> findByChannelIds(List<String> channelIds, int limit) {
        // In production, this would query video-catalog-service with channel filter
        log.info("Finding videos for channels={}, limit={}", channelIds, limit);
        return generateMockVideos(limit);
    }
    
    @Override
    public List<FeedItem> findTrending(int limit, String category) {
        // In production, this would query trending videos from analytics
        log.info("Finding trending videos, limit={}, category={}", limit, category);
        return generateMockVideos(limit);
    }
    
    private List<FeedItem> generateMockVideos(int count) {
        List<FeedItem> videos = new ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            videos.add(FeedItem.builder()
                    .videoId("video-" + System.currentTimeMillis() + "-" + i)
                    .title("Sample Video #" + i)
                    .channelId("channel-" + random.nextInt(100))
                    .channelName("Channel " + random.nextInt(100))
                    .thumbnailUrl("https://via.placeholder.com/640x480?text=Video+" + i)
                    .publishedAt(Instant.now().minusSeconds(random.nextInt(86400)))
                    .viewCount((long) random.nextInt(1000000))
                    .durationSeconds((long) (60 + random.nextInt(300)))
                    .isAd(false)
                    .adSlotIndex(null)
                    .category("entertainment")
                    .build());
        }
        
        return videos;
    }
}

