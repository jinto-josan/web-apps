package com.youtube.mvp.feeds.infrastructure.repository.adapter;

import com.youtube.mvp.feeds.domain.repository.SubscriptionRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Random;

@Slf4j
@Repository
public class SubscriptionRepositoryAdapter implements SubscriptionRepository {
    
    private final Random random = new Random();
    
    @Override
    public List<String> findSubscribedChannels(String userId) {
        // In production, this would query channel-service
        log.info("Finding subscribed channels for userId={}", userId);
        
        // Mock: return 5-10 random channels
        int count = 5 + random.nextInt(6);
        List<String> channels = new java.util.ArrayList<>();
        
        for (int i = 0; i < count; i++) {
            channels.add("channel-" + random.nextInt(100));
        }
        
        return channels;
    }
    
    @Override
    public boolean isSubscribed(String userId, String channelId) {
        // In production, this would check subscription status
        log.info("Checking subscription for userId={}, channelId={}", userId, channelId);
        return random.nextBoolean();
    }
}

