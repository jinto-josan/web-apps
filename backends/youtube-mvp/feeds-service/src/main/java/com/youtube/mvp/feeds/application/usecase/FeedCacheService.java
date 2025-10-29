package com.youtube.mvp.feeds.application.usecase;

import com.youtube.mvp.feeds.domain.model.Feed;
import com.youtube.mvp.feeds.domain.model.FeedType;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.CachePut;
import org.springframework.stereotype.Service;

@Service
public class FeedCacheService {
    
    @CachePut(value = "feeds", key = "#userId + ':' + #feedType")
    public Feed cacheFeed(String userId, FeedType feedType, Feed feed) {
        return feed;
    }
    
    @CacheEvict(value = "feeds", key = "#userId + ':' + #feedType")
    public void evictFeed(String userId, FeedType feedType) {
        // Cache eviction logic
    }
    
    @CacheEvict(value = "feeds", allEntries = true)
    public void evictAllFeeds() {
        // Clear all cache
    }
}

