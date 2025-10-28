package com.youtube.channelservice.application.usecases;

import com.github.f4b6a3.ulid.UlidCreator;
import com.youtube.channelservice.application.commands.SubscribeToChannelCommand;
import com.youtube.channelservice.application.commands.UnsubscribeFromChannelCommand;
import com.youtube.channelservice.application.queries.GetChannelSubscriptionStatsQuery;
import com.youtube.channelservice.application.queries.GetUserSubscriptionsQuery;
import com.youtube.channelservice.domain.events.SubscriptionCreated;
import com.youtube.channelservice.domain.events.SubscriptionDeleted;
import com.youtube.channelservice.domain.models.ChannelSubscriptionStats;
import com.youtube.channelservice.domain.models.Subscription;
import com.youtube.channelservice.domain.models.UserSubscriptionInfo;
import com.youtube.channelservice.domain.repositories.ChannelSubscriptionStatsRepository;
import com.youtube.channelservice.domain.repositories.IdempotencyRepository;
import com.youtube.channelservice.domain.repositories.SubscriptionRepository;
import com.youtube.channelservice.domain.services.EventPublisher;
import com.youtube.channelservice.shared.exceptions.ConflictException;
import com.youtube.channelservice.shared.exceptions.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class SubscriptionUseCaseImpl implements SubscriptionUseCase {
    
    private final SubscriptionRepository subscriptionRepository;
    private final ChannelSubscriptionStatsRepository statsRepository;
    private final IdempotencyRepository idempotencyRepository;
    private final EventPublisher eventPublisher;
    
    @Override
    @Transactional
    public Subscription subscribeToChannel(SubscribeToChannelCommand command) {
        log.info("Subscribing user {} to channel {}", command.getUserId(), command.getChannelId());
        
        // Idempotency check
        Optional<String> cachedResponse = idempotencyRepository.get(command.getIdempotencyKey());
        if (cachedResponse.isPresent()) {
            log.info("Idempotent request detected, returning cached response");
            // Return cached subscription
            return subscriptionRepository.findByUserIdAndChannelId(command.getUserId(), command.getChannelId())
                .orElseThrow(() -> new NotFoundException("Subscription not found"));
        }
        
        // Check if already subscribed
        Optional<Subscription> existing = subscriptionRepository.findByUserIdAndChannelId(
            command.getUserId(), command.getChannelId()
        );
        
        if (existing.isPresent()) {
            Subscription sub = existing.get();
            if (sub.getIsActive()) {
                throw new ConflictException("User is already subscribed to this channel");
            }
            // Reactivate subscription
            sub.setIsActive(true);
            sub.setCreatedAt(Instant.now());
            subscriptionRepository.save(sub);
            
            // Cache idempotency
            idempotencyRepository.put(command.getIdempotencyKey(), sub.getId(), Duration.ofHours(24));
            
            // Publish event
            publishSubscriptionCreated(sub);
            
            return sub;
        }
        
        // Create new subscription
        String subscriptionId = UlidCreator.getUlid().toString();
        String shardSuffix = Subscription.calculateShardSuffix(command.getUserId());
        
        Subscription.SubscriptionNotificationPreference prefs = Subscription.SubscriptionNotificationPreference.builder()
            .notifyOnUpload(command.getNotifyOnUpload() != null ? command.getNotifyOnUpload() : true)
            .notifyOnLive(command.getNotifyOnLive() != null ? command.getNotifyOnLive() : true)
            .notifyOnCommunityPost(command.getNotifyOnCommunityPost() != null ? command.getNotifyOnCommunityPost() : true)
            .notifyOnShorts(command.getNotifyOnShorts() != null ? command.getNotifyOnShorts() : true)
            .build();
        
        Subscription subscription = Subscription.builder()
            .id(subscriptionId)
            .userId(command.getUserId())
            .channelId(command.getChannelId())
            .shardSuffix(shardSuffix)
            .createdAt(Instant.now())
            .isActive(true)
            .notificationPreference(prefs)
            .build();
        
        subscription = subscriptionRepository.save(subscription);
        
        // Update stats
        statsRepository.incrementSubscriberCount(command.getChannelId());
        
        // Cache idempotency
        idempotencyRepository.put(command.getIdempotencyKey(), subscription.getId(), Duration.ofHours(24));
        
        // Publish event
        publishSubscriptionCreated(subscription);
        
        log.info("Successfully subscribed user {} to channel {}", command.getUserId(), command.getChannelId());
        return subscription;
    }
    
    @Override
    @Transactional
    public void unsubscribeFromChannel(UnsubscribeFromChannelCommand command) {
        log.info("Unsubscribing user {} from channel {}", command.getUserId(), command.getChannelId());
        
        // Idempotency check
        Optional<String> cachedResponse = idempotencyRepository.get(command.getIdempotencyKey());
        if (cachedResponse.isPresent()) {
            log.info("Idempotent request detected, already processed");
            return;
        }
        
        Subscription subscription = subscriptionRepository.findByUserIdAndChannelId(
            command.getUserId(), command.getChannelId()
        ).orElseThrow(() -> new NotFoundException("Subscription not found"));
        
        if (!subscription.getIsActive()) {
            // Already unsubscribed, cache and return
            idempotencyRepository.put(command.getIdempotencyKey(), "unsubscribed", Duration.ofHours(24));
            return;
        }
        
        subscription.setIsActive(false);
        subscriptionRepository.save(subscription);
        
        // Update stats
        statsRepository.decrementSubscriberCount(command.getChannelId());
        
        // Cache idempotency
        idempotencyRepository.put(command.getIdempotencyKey(), "unsubscribed", Duration.ofHours(24));
        
        // Publish event
        publishSubscriptionDeleted(subscription, command.getReason());
        
        log.info("Successfully unsubscribed user {} from channel {}", command.getUserId(), command.getChannelId());
    }
    
    @Override
    public UserSubscriptionInfo getUserSubscriptions(GetUserSubscriptionsQuery query) {
        log.info("Getting subscriptions for user {}", query.getUserId());
        
        String shardSuffix = query.getShardSuffix() != null 
            ? query.getShardSuffix() 
            : Subscription.calculateShardSuffix(query.getUserId());
        
        List<Subscription> subscriptions = subscriptionRepository.findByUserIdWithShard(
            query.getUserId(), shardSuffix, query.getOffset(), query.getLimit()
        );
        
        if (query.getIncludeInactive() == null || !query.getIncludeInactive()) {
            subscriptions = subscriptions.stream()
                .filter(Subscription::getIsActive)
                .collect(Collectors.toList());
        }
        
        List<UserSubscriptionInfo.SubscriptionSummary> summaries = subscriptions.stream()
            .map(sub -> UserSubscriptionInfo.SubscriptionSummary.builder()
                .channelId(sub.getChannelId())
                .subscribedAt(sub.getCreatedAt())
                .notificationPreference(sub.getNotificationPreference())
                .build())
            .collect(Collectors.toList());
        
        long totalCount = subscriptionRepository.countByUserId(query.getUserId());
        
        return UserSubscriptionInfo.builder()
            .userId(query.getUserId())
            .subscriptions(summaries)
            .totalCount(totalCount)
            .updatedAt(Instant.now())
            .build();
    }
    
    @Override
    public ChannelSubscriptionStats getChannelSubscriptionStats(GetChannelSubscriptionStatsQuery query) {
        log.info("Getting subscription stats for channel {}", query.getChannelId());
        
        return statsRepository.findByChannelId(query.getChannelId())
            .orElseGet(() -> ChannelSubscriptionStats.builder()
                .channelId(query.getChannelId())
                .subscriberCount(0L)
                .activeSubscriberCount(0L)
                .build());
    }
    
    @Override
    public boolean isSubscribed(String userId, String channelId) {
        return subscriptionRepository.findByUserIdAndChannelId(userId, channelId)
            .map(Subscription::getIsActive)
            .orElse(false);
    }
    
    private void publishSubscriptionCreated(Subscription subscription) {
        try {
            SubscriptionCreated event = SubscriptionCreated.builder()
                .subscriptionId(subscription.getId())
                .userId(subscription.getUserId())
                .channelId(subscription.getChannelId())
                .subscribedAt(subscription.getCreatedAt())
                .shardSuffix(subscription.getShardSuffix())
                .build();
            
            eventPublisher.publish("subscription.created", event);
            log.debug("Published subscription.created event for subscription {}", subscription.getId());
        } catch (Exception e) {
            log.error("Failed to publish subscription.created event", e);
            // Don't fail the transaction, use outbox pattern
        }
    }
    
    private void publishSubscriptionDeleted(Subscription subscription, String reason) {
        try {
            SubscriptionDeleted event = SubscriptionDeleted.builder()
                .subscriptionId(subscription.getId())
                .userId(subscription.getUserId())
                .channelId(subscription.getChannelId())
                .unsubscribedAt(Instant.now())
                .reason(reason)
                .build();
            
            eventPublisher.publish("subscription.deleted", event);
            log.debug("Published subscription.deleted event for subscription {}", subscription.getId());
        } catch (Exception e) {
            log.error("Failed to publish subscription.deleted event", e);
            // Don't fail the transaction, use outbox pattern
        }
    }
}
