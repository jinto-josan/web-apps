package com.youtube.channelservice.infrastructure.persistence.impl;

import com.youtube.channelservice.domain.models.ChannelSubscriptionStats;
import com.youtube.channelservice.domain.repositories.ChannelSubscriptionStatsRepository;
import com.youtube.channelservice.infrastructure.persistence.entity.ChannelSubscriptionStatsEntity;
import com.youtube.channelservice.infrastructure.persistence.repository.CosmosChannelSubscriptionStatsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Slf4j
@Repository
@RequiredArgsConstructor
public class ChannelSubscriptionStatsRepositoryImpl implements ChannelSubscriptionStatsRepository {
    
    private final CosmosChannelSubscriptionStatsRepository cosmosRepository;
    
    @Override
    public Optional<ChannelSubscriptionStats> findByChannelId(String channelId) {
        return Optional.ofNullable(cosmosRepository.findByChannelId(channelId))
            .map(entity -> ChannelSubscriptionStats.builder()
                .channelId(entity.getChannelId())
                .subscriberCount(entity.getSubscriberCount())
                .activeSubscriberCount(entity.getActiveSubscriberCount())
                .lastUpdatedAt(entity.getLastUpdatedAt())
                .lastSubscriberAddedAt(entity.getLastSubscriberAddedAt())
                .build());
    }
    
    @Override
    public ChannelSubscriptionStats save(ChannelSubscriptionStats stats) {
        ChannelSubscriptionStatsEntity entity = ChannelSubscriptionStatsEntity.builder()
            .channelId(stats.getChannelId())
            .subscriberCount(stats.getSubscriberCount())
            .activeSubscriberCount(stats.getActiveSubscriberCount())
            .lastUpdatedAt(stats.getLastUpdatedAt())
            .lastSubscriberAddedAt(stats.getLastSubscriberAddedAt())
            .build();
        
        ChannelSubscriptionStatsEntity saved = cosmosRepository.save(entity);
        
        return ChannelSubscriptionStats.builder()
            .channelId(saved.getChannelId())
            .subscriberCount(saved.getSubscriberCount())
            .activeSubscriberCount(saved.getActiveSubscriberCount())
            .lastUpdatedAt(saved.getLastUpdatedAt())
            .lastSubscriberAddedAt(saved.getLastSubscriberAddedAt())
            .build();
    }
    
    @Override
    public void incrementSubscriberCount(String channelId) {
        ChannelSubscriptionStatsEntity entity = cosmosRepository.findByChannelId(channelId);
        
        if (entity == null) {
            entity = ChannelSubscriptionStatsEntity.builder()
                .channelId(channelId)
                .subscriberCount(1L)
                .activeSubscriberCount(1L)
                .lastUpdatedAt(Instant.now())
                .lastSubscriberAddedAt(Instant.now())
                .build();
        } else {
            entity.setSubscriberCount(
                entity.getSubscriberCount() != null ? entity.getSubscriberCount() + 1 : 1L
            );
            entity.setActiveSubscriberCount(
                entity.getActiveSubscriberCount() != null ? entity.getActiveSubscriberCount() + 1 : 1L
            );
            entity.setLastUpdatedAt(Instant.now());
            entity.setLastSubscriberAddedAt(Instant.now());
        }
        
        cosmosRepository.save(entity);
    }
    
    @Override
    public void decrementSubscriberCount(String channelId) {
        ChannelSubscriptionStatsEntity entity = cosmosRepository.findByChannelId(channelId);
        
        if (entity != null && entity.getSubscriberCount() != null && entity.getSubscriberCount() > 0) {
            entity.setSubscriberCount(entity.getSubscriberCount() - 1);
            
            if (entity.getActiveSubscriberCount() != null && entity.getActiveSubscriberCount() > 0) {
                entity.setActiveSubscriberCount(entity.getActiveSubscriberCount() - 1);
            }
            
            entity.setLastUpdatedAt(Instant.now());
            cosmosRepository.save(entity);
        }
    }
}
