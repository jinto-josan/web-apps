package com.youtube.recommendationsservice.infrastructure.persistence;

import com.youtube.recommendationsservice.domain.entities.UserFeatures;
import com.youtube.recommendationsservice.domain.repositories.UserFeaturesRepository;
import com.youtube.recommendationsservice.domain.valueobjects.FeatureVector;
import com.youtube.recommendationsservice.domain.valueobjects.UserId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class UserFeaturesRepositoryAdapter implements UserFeaturesRepository {
    
    private final UserFeaturesJpaRepositoryInterface jpaRepository;
    
    @Override
    public Optional<UserFeatures> findByUserId(UserId userId) {
        return jpaRepository.findById(userId.getValue())
            .map(this::toDomain);
    }
    
    @Override
    public void save(UserFeatures userFeatures) {
        UserFeaturesJpaRepository entity = toEntity(userFeatures);
        jpaRepository.save(entity);
    }
    
    @Override
    public boolean exists(UserId userId) {
        return jpaRepository.existsById(userId.getValue());
    }
    
    private UserFeatures toDomain(UserFeaturesJpaRepository entity) {
        FeatureVector featureVector = FeatureVector.builder()
            .embeddings(entity.getEmbeddings())
            .categoricalFeatures(entity.getCategoricalFeatures())
            .numericalFeatures(entity.getNumericalFeatures())
            .build();
        
        return UserFeatures.builder()
            .userId(UserId.from(entity.getUserId()))
            .features(featureVector)
            .recentlyViewedCategories(entity.getRecentlyViewedCategories())
            .preferredLanguages(entity.getPreferredLanguages())
            .lastUpdated(entity.getLastUpdated())
            .build();
    }
    
    private UserFeaturesJpaRepository toEntity(UserFeatures userFeatures) {
        UserFeaturesJpaRepository entity = new UserFeaturesJpaRepository();
        entity.setUserId(userFeatures.getUserId().getValue());
        entity.setEmbeddings(userFeatures.getFeatures().getEmbeddings());
        entity.setCategoricalFeatures(userFeatures.getFeatures().getCategoricalFeatures());
        entity.setNumericalFeatures(userFeatures.getFeatures().getNumericalFeatures());
        entity.setRecentlyViewedCategories(userFeatures.getRecentlyViewedCategories());
        entity.setPreferredLanguages(userFeatures.getPreferredLanguages());
        entity.setLastUpdated(userFeatures.getLastUpdated());
        return entity;
    }
}

