package com.youtube.channelservice.infrastructure.persistence.impl;

import com.youtube.channelservice.domain.models.Subscription;
import com.youtube.channelservice.domain.repositories.SubscriptionRepository;
import com.youtube.channelservice.infrastructure.persistence.entity.SubscriptionEntity;
import com.youtube.channelservice.infrastructure.persistence.mapper.SubscriptionMapper;
import com.youtube.channelservice.infrastructure.persistence.repository.CosmosSubscriptionRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Repository
@RequiredArgsConstructor
public class SubscriptionRepositoryImpl implements SubscriptionRepository {
    
    private final CosmosSubscriptionRepository cosmosRepository;
    private final SubscriptionMapper mapper;
    private final ObjectMapper objectMapper;
    
    @Override
    public Optional<Subscription> findById(String subscriptionId) {
        return cosmosRepository.findById(subscriptionId)
            .map(this::toDomain);
    }
    
    @Override
    public List<Subscription> findByUserIdWithShard(String userId, String shardSuffix, int offset, int limit) {
        return cosmosRepository.findByUserIdAndShardSuffix(userId, shardSuffix)
            .stream()
            .skip(offset)
            .limit(limit)
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public Optional<Subscription> findByUserIdAndChannelId(String userId, String channelId) {
        return cosmosRepository.findByUserIdAndChannelId(userId, channelId)
            .map(this::toDomain);
    }
    
    @Override
    public List<Subscription> findByChannelId(String channelId, int offset, int limit) {
        return cosmosRepository.findByChannelId(channelId)
            .stream()
            .skip(offset)
            .limit(limit)
            .map(this::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public long countByUserId(String userId) {
        // Note: Cosmos doesn't support count queries efficiently, 
        // may need to maintain a counter or use change feed
        String shardSuffix = Subscription.calculateShardSuffix(userId);
        return cosmosRepository.findByUserIdAndShardSuffix(userId, shardSuffix).size();
    }
    
    @Override
    public long countByChannelId(String channelId) {
        return cosmosRepository.countByChannelId(channelId);
    }
    
    @Override
    public Subscription save(Subscription subscription) {
        SubscriptionEntity entity = toEntity(subscription);
        entity.setUpdatedAt(java.time.Instant.now());
        SubscriptionEntity saved = cosmosRepository.save(entity);
        return toDomain(saved);
    }
    
    @Override
    public void delete(Subscription subscription) {
        cosmosRepository.deleteById(subscription.getId());
    }
    
    @Override
    public void deleteById(String subscriptionId) {
        cosmosRepository.deleteById(subscriptionId);
    }
    
    private SubscriptionEntity toEntity(Subscription subscription) {
        SubscriptionEntity entity = SubscriptionEntity.builder()
            .id(subscription.getId())
            .userId(subscription.getUserId())
            .channelId(subscription.getChannelId())
            .shardSuffix(subscription.getShardSuffix())
            .createdAt(subscription.getCreatedAt())
            .updatedAt(java.time.Instant.now())
            .isActive(subscription.getIsActive())
            .build();
        
        // Serialize notification preferences to JSON
        try {
            if (subscription.getNotificationPreference() != null) {
                entity.setNotificationPreferenceJson(
                    objectMapper.writeValueAsString(subscription.getNotificationPreference())
                );
            }
        } catch (Exception e) {
            log.error("Failed to serialize notification preferences", e);
        }
        
        return entity;
    }
    
    private Subscription toDomain(SubscriptionEntity entity) {
        Subscription.SubscriptionNotificationPreference prefs = null;
        
        try {
            if (entity.getNotificationPreferenceJson() != null) {
                prefs = objectMapper.readValue(
                    entity.getNotificationPreferenceJson(),
                    Subscription.SubscriptionNotificationPreference.class
                );
            }
        } catch (Exception e) {
            log.error("Failed to deserialize notification preferences", e);
        }
        
        return Subscription.builder()
            .id(entity.getId())
            .userId(entity.getUserId())
            .channelId(entity.getChannelId())
            .shardSuffix(entity.getShardSuffix())
            .createdAt(entity.getCreatedAt())
            .isActive(entity.getIsActive())
            .notificationPreference(prefs)
            .build();
    }
}
