package com.youtube.experimentationservice.infrastructure.adapters.cosmos;

import com.youtube.experimentationservice.domain.model.Experiment;
import com.youtube.experimentationservice.domain.repositories.ExperimentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Repository
@RequiredArgsConstructor
public class ExperimentRepositoryAdapter implements ExperimentRepository {
    private final ExperimentCosmosRepository cosmosRepository;

    @Override
    public Optional<Experiment> findById(String id) {
        return cosmosRepository.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Experiment> findByKey(String key) {
        return cosmosRepository.findByKey(key).map(this::toDomain);
    }

    @Override
    public List<Experiment> findAllActive() {
        return StreamSupport.stream(cosmosRepository.findAll().spliterator(), false)
                .filter(e -> "ACTIVE".equals(e.getStatus()))
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public Experiment save(Experiment experiment) {
        ExperimentCosmosEntity entity = toEntity(experiment);
        ExperimentCosmosEntity saved = cosmosRepository.save(entity);
        return toDomain(saved);
    }

    @Override
    public void deleteById(String id) {
        cosmosRepository.deleteById(id);
    }

    private Experiment toDomain(ExperimentCosmosEntity entity) {
        return Experiment.builder()
                .id(entity.getId())
                .key(entity.getKey())
                .name(entity.getName())
                .status(Experiment.ExperimentStatus.valueOf(entity.getStatus()))
                .variants(entity.getVariants().stream()
                        .map(v -> Experiment.Variant.builder()
                                .id(v.getId())
                                .name(v.getName())
                                .trafficPercentage(v.getTrafficPercentage())
                                .configuration(v.getConfiguration())
                                .build())
                        .collect(Collectors.toList()))
                .rolloutPercentage(entity.getRolloutPercentage())
                .conditions(entity.getConditions())
                .assignmentStrategy(Experiment.AssignmentStrategy.valueOf(entity.getAssignmentStrategy()))
                .startDate(entity.getStartDate())
                .endDate(entity.getEndDate())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    private ExperimentCosmosEntity toEntity(Experiment experiment) {
        return ExperimentCosmosEntity.builder()
                .id(experiment.getId())
                .key(experiment.getKey())
                .name(experiment.getName())
                .status(experiment.getStatus().name())
                .variants(experiment.getVariants().stream()
                        .map(v -> ExperimentCosmosEntity.VariantEntity.builder()
                                .id(v.getId())
                                .name(v.getName())
                                .trafficPercentage(v.getTrafficPercentage())
                                .configuration(v.getConfiguration())
                                .build())
                        .collect(Collectors.toList()))
                .rolloutPercentage(experiment.getRolloutPercentage())
                .conditions(experiment.getConditions())
                .assignmentStrategy(experiment.getAssignmentStrategy().name())
                .startDate(experiment.getStartDate())
                .endDate(experiment.getEndDate())
                .createdAt(experiment.getCreatedAt())
                .updatedAt(experiment.getUpdatedAt())
                .build();
    }
}

