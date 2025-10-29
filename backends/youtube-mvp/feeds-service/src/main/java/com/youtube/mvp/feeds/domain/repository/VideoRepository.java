package com.youtube.mvp.feeds.domain.repository;

import com.youtube.mvp.feeds.domain.model.FeedItem;

import java.util.List;

public interface VideoRepository {
    List<FeedItem> findRecentPublished(int limit);
    
    List<FeedItem> findRecommendedForUser(String userId, int limit);
    
    List<FeedItem> findByChannelIds(List<String> channelIds, int limit);
    
    List<FeedItem> findTrending(int limit, String category);
}

