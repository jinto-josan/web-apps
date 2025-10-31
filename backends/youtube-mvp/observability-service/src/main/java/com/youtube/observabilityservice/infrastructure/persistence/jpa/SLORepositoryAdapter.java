package com.youtube.observabilityservice.infrastructure.persistence.jpa;

import com.youtube.observabilityservice.domain.entities.SLO;
import com.youtube.observabilityservice.domain.entities.SLI;
import com.youtube.observabilityservice.domain.repositories.SLORepository;
import com.youtube.observabilityservice.domain.valueobjects.SLOId;
import com.youtube.observabilityservice.domain.valueobjects.TimeWindow;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class SLORepositoryAdapter implements SLORepository {
    
    private final JpaSLORepository jpaRepository;
    
    @Override
    public SLO save(SLO slo) {
        SLOEntity entity = toEntity(slo);
        SLOEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }
    
    @Override
    public Optional<SLO> findById(SLOId id) {
        return jpaRepository.findById(id.getValue())
                .map(this::toDomain);
    }
    
    @Override
    public List<SLO> findByServiceName(String serviceName) {
        return jpaRepository.findByServiceName(serviceName).stream()
                .map(this::toDomain)
                .toList();
    }
    
    @Override
    public List<SLO> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }
    
    @Override
    public void deleteById(SLOId id) {
        jpaRepository.deleteById(id.getValue());
    }
    
    private SLOEntity toEntity(SLO slo) {
        SLOEntity entity = new SLOEntity();
        entity.setId(slo.getId().getValue());
        entity.setName(slo.getName());
        entity.setServiceName(slo.getServiceName());
        entity.setDescription(slo.getDescription());
        entity.setSlis(slo.getSlis().stream()
                .map(sli -> {
                    SLIEmbeddable embeddable = new SLIEmbeddable();
                    embeddable.setName(sli.getName());
                    embeddable.setType(sli.getType().name());
                    embeddable.setQuery(sli.getQuery());
                    embeddable.setLastCalculatedAt(sli.getLastCalculatedAt());
                    embeddable.setLastValue(sli.getLastValue());
                    return embeddable;
                })
                .collect(Collectors.toList()));
        entity.setTargetPercent(slo.getTargetPercent());
        entity.setTimeWindowDuration(slo.getTimeWindow().getDuration().toString());
        entity.setTimeWindowType(slo.getTimeWindow().getType().name());
        entity.setErrorBudget(slo.getErrorBudget());
        entity.setErrorBudgetRemaining(slo.getErrorBudgetRemaining());
        entity.setLabels(slo.getLabels());
        entity.setCreatedAt(slo.getCreatedAt());
        entity.setUpdatedAt(slo.getUpdatedAt());
        return entity;
    }
    
    private SLO toDomain(SLOEntity entity) {
        List<SLI> slis = entity.getSlis().stream()
                .map(embeddable -> SLI.builder()
                        .name(embeddable.getName())
                        .type(SLI.SLIType.valueOf(embeddable.getType()))
                        .query(embeddable.getQuery())
                        .lastCalculatedAt(embeddable.getLastCalculatedAt())
                        .lastValue(embeddable.getLastValue())
                        .build())
                .collect(Collectors.toList());
        
        TimeWindow timeWindow = TimeWindow.TimeWindowType.valueOf(entity.getTimeWindowType()) == TimeWindow.TimeWindowType.ROLLING
                ? TimeWindow.rollingDays((int) java.time.Duration.parse(entity.getTimeWindowDuration()).toDays())
                : TimeWindow.calendarDays((int) java.time.Duration.parse(entity.getTimeWindowDuration()).toDays());
        
        return SLO.builder()
                .id(new SLOId(entity.getId()))
                .name(entity.getName())
                .serviceName(entity.getServiceName())
                .description(entity.getDescription())
                .slis(slis)
                .targetPercent(entity.getTargetPercent())
                .timeWindow(timeWindow)
                .errorBudget(entity.getErrorBudget())
                .errorBudgetRemaining(entity.getErrorBudgetRemaining())
                .labels(entity.getLabels())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

