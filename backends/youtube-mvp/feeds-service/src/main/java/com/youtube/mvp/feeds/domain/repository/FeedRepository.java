package com.youtube.mvp.feeds.domain.repository;

import com.youtube.mvp.feeds.domain.model.Feed;
import com.youtube.mvp.feeds.domain.model.FeedType;
import com.youtube.mvp.feeds.domain.model.FeedView;

import java.util.List;
import java.util.Optional;

public interface FeedRepository {
    Optional<Feed> findByUserIdAndFeedType(String userId, FeedType feedType);
    
    void save(Feed feed);
    
    void saveFeedView(FeedView feedView);
    
    List<FeedView> findRecentViewsByUserId(String userId, int limit);
    
    void deleteByUserIdAndFeedType(String userId, FeedType feedType);
    
    void deleteAllByUserId(String userId);
}

