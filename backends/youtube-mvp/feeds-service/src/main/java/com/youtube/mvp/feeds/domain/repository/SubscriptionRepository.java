package com.youtube.mvp.feeds.domain.repository;

import java.util.List;

public interface SubscriptionRepository {
    List<String> findSubscribedChannels(String userId);
    
    boolean isSubscribed(String userId, String channelId);
}

