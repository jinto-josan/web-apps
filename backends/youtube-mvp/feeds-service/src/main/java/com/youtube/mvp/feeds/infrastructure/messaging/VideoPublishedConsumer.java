package com.youtube.mvp.feeds.infrastructure.messaging;

import com.youtube.mvp.feeds.application.usecase.FeedCacheService;
import com.youtube.mvp.feeds.domain.model.FeedType;
import com.youtube.mvp.feeds.domain.repository.FeedRepository;
import com.youtube.mvp.feeds.domain.repository.SubscriptionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.azure.servicebus.consumer.ServiceBusMessageListenerContainer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.Message;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.messaging.handler.annotation.Payload;

import java.util.List;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class VideoPublishedConsumer {
    
    private final FeedRepository feedRepository;
    private final SubscriptionRepository subscriptionRepository;
    private final FeedCacheService feedCacheService;
    
    @Bean
    public ServiceBusMessageListenerContainer messageListenerContainer() {
        return new ServiceBusMessageListenerContainer("video-published", null);
    }
    
    public void handleVideoPublished(
            @Payload VideoPublishedEvent event,
            @Header("Idempotency-Key") String idempotencyKey) {
        
        log.info("Received video.published event: videoId={}, channelId={}, idempotencyKey={}", 
                event.getVideoId(), event.getChannelId(), idempotencyKey);
        
        try {
            // Find all users subscribed to this channel
            List<String> subscribedUserIds = subscriptionRepository.findSubscribedChannels(event.getChannelId());
            
            if (subscribedUserIds.isEmpty()) {
                log.debug("No subscribers found for channelId={}", event.getChannelId());
                return;
            }
            
            // Fan-out: update feeds for all subscribed users
            subscribedUserIds.parallelStream().forEach(userId -> {
                updateUserFeed(userId, FeedType.SUBSCRIPTIONS, idempotencyKey);
            });
            
            log.info("Fan-out completed for videoId={}, updated {} user feeds", 
                    event.getVideoId(), subscribedUserIds.size());
            
        } catch (Exception e) {
            log.error("Error processing video.published event for videoId={}", event.getVideoId(), e);
            throw new RuntimeException("Failed to process video published event", e);
        }
    }
    
    private void updateUserFeed(String userId, FeedType feedType, String idempotencyKey) {
        // Evict cache for this user's feed
        feedCacheService.evictFeed(userId, feedType);
        
        // In production, trigger feed regeneration async
        log.debug("Evicted feed cache for userId={}, feedType={}, idempotencyKey={}", 
                userId, feedType, idempotencyKey);
    }
}

