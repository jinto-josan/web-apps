package com.youtube.experimentationservice.infrastructure.adapters.cosmos;

import com.github.f4b6a3.ulid.UlidCreator;
import com.youtube.experimentationservice.domain.model.UserCohort;
import com.youtube.experimentationservice.domain.repositories.CohortRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class CohortRepositoryAdapter implements CohortRepository {
    private final CohortCosmosRepository cosmosRepository;

    @Override
    public Optional<UserCohort> findByUserIdAndExperimentKey(String userId, String experimentKey) {
        return cosmosRepository.findByUserIdAndExperimentKey(userId, experimentKey)
                .map(this::toDomain);
    }

    @Override
    public UserCohort save(UserCohort cohort) {
        CohortCosmosEntity entity = toEntity(cohort);
        CohortCosmosEntity saved = cosmosRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteByUserIdAndExperimentKey(String userId, String experimentKey) {
        cosmosRepository.findByUserIdAndExperimentKey(userId, experimentKey)
                .ifPresent(cosmosRepository::delete);
    }

    private UserCohort toDomain(CohortCosmosEntity entity) {
        return UserCohort.builder()
                .userId(entity.getUserId())
                .experimentKey(entity.getExperimentKey())
                .variantId(entity.getVariantId())
                .assignedAt(entity.getAssignedAt())
                .metadata(entity.getMetadata())
                .build();
    }

    private CohortCosmosEntity toEntity(UserCohort cohort) {
        return CohortCosmosEntity.builder()
                .id(cohort.getUserId() + ":" + cohort.getExperimentKey())
                .userId(cohort.getUserId())
                .experimentKey(cohort.getExperimentKey())
                .variantId(cohort.getVariantId())
                .assignedAt(cohort.getAssignedAt())
                .metadata(cohort.getMetadata())
                .build();
    }
}

