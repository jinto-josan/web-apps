package com.youtube.antiaabuseservice.infrastructure.adapters.cosmos;

import com.youtube.antiaabuseservice.domain.model.FeatureStore;
import com.youtube.antiaabuseservice.domain.repositories.FeatureStoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class FeatureStoreRepositoryAdapter implements FeatureStoreRepository {
    private final FeatureStoreCosmosRepository cosmosRepository;

    @Override
    public Optional<FeatureStore> findByUserIdAndFeatureSet(String userId, String featureSet) {
        return cosmosRepository.findByUserIdAndFeatureSet(userId, featureSet)
                .map(this::toDomain);
    }

    @Override
    public FeatureStore save(FeatureStore featureStore) {
        FeatureStoreCosmosEntity entity = toEntity(featureStore);
        FeatureStoreCosmosEntity saved = cosmosRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteByUserId(String userId) {
        // Implementation if needed
    }

    private FeatureStore toDomain(FeatureStoreCosmosEntity entity) {
        return FeatureStore.builder()
                .userId(entity.getUserId())
                .featureSet(entity.getFeatureSet())
                .features(entity.getFeatures())
                .lastUpdated(entity.getLastUpdated())
                .expiresAt(entity.getExpiresAt())
                .build();
    }

    private FeatureStoreCosmosEntity toEntity(FeatureStore featureStore) {
        return FeatureStoreCosmosEntity.builder()
                .id(featureStore.getUserId() + ":" + featureStore.getFeatureSet())
                .userId(featureStore.getUserId())
                .featureSet(featureStore.getFeatureSet())
                .features(featureStore.getFeatures())
                .lastUpdated(featureStore.getLastUpdated())
                .expiresAt(featureStore.getExpiresAt())
                .build();
    }
}

