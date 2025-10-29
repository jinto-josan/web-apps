package com.youtube.mvp.feeds.application.usecase;

import com.youtube.mvp.feeds.domain.model.FeedItem;
import com.youtube.mvp.feeds.domain.model.FeedType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Slf4j
@Service
public class AdSlotService {
    
    private static final int AD_SLOT_INTERVAL = 10; // Inject ad every 10 items
    private final Random random = new Random();
    
    public List<FeedItem> injectAds(List<FeedItem> items, FeedType feedType) {
        if (items == null || items.isEmpty()) {
            return items;
        }
        
        List<FeedItem> result = new ArrayList<>(items);
        
        // Inject ads at regular intervals
        for (int i = AD_SLOT_INTERVAL; i < result.size(); i += AD_SLOT_INTERVAL) {
            FeedItem ad = createAdItem(i / AD_SLOT_INTERVAL, feedType);
            result.add(i, ad);
        }
        
        log.debug("Injected {} ads into feed of type {}", result.size() - items.size(), feedType);
        return result;
    }
    
    private FeedItem createAdItem(int slotIndex, FeedType feedType) {
        // In production, this would fetch real ad from ad service
        String adId = "ad-" + slotIndex + "-" + System.currentTimeMillis();
        
        return FeedItem.builder()
                .videoId(adId)
                .title("Sponsored Ad #" + slotIndex)
                .channelId("ad-channel-" + random.nextInt(1000))
                .channelName("Advertiser")
                .thumbnailUrl("https://via.placeholder.com/640x480?text=Ad")
                .publishedAt(java.time.Instant.now())
                .viewCount(0L)
                .durationSeconds(15L + random.nextInt(45))
                .isAd(true)
                .adSlotIndex(slotIndex)
                .category("AD")
                .build();
    }
}

