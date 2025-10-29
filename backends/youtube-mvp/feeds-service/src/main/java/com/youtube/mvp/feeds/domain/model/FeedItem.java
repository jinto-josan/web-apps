package com.youtube.mvp.feeds.domain.model;

import lombok.Builder;
import lombok.Value;

import java.time.Instant;

@Value
@Builder
public class FeedItem {
    String videoId;
    String title;
    String channelId;
    String channelName;
    String thumbnailUrl;
    Instant publishedAt;
    Long viewCount;
    Long durationSeconds;
    boolean isAd;
    Integer adSlotIndex;
    String category;
}

