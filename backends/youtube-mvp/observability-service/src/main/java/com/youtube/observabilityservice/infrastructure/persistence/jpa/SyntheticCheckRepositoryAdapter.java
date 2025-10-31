package com.youtube.observabilityservice.infrastructure.persistence.jpa;

import com.youtube.observabilityservice.domain.entities.SyntheticCheck;
import com.youtube.observabilityservice.domain.entities.SyntheticCheckResult;
import com.youtube.observabilityservice.domain.repositories.SyntheticCheckRepository;
import com.youtube.observabilityservice.domain.valueobjects.SyntheticCheckId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class SyntheticCheckRepositoryAdapter implements SyntheticCheckRepository {
    
    private final JpaSyntheticCheckRepository jpaRepository;
    
    @Override
    public SyntheticCheck save(SyntheticCheck check) {
        SyntheticCheckEntity entity = toEntity(check);
        SyntheticCheckEntity saved = jpaRepository.save(entity);
        return toDomain(saved);
    }
    
    @Override
    public Optional<SyntheticCheck> findById(SyntheticCheckId id) {
        return jpaRepository.findById(id.getValue())
                .map(this::toDomain);
    }
    
    @Override
    public List<SyntheticCheck> findAll() {
        return jpaRepository.findAll().stream()
                .map(this::toDomain)
                .toList();
    }
    
    @Override
    public List<SyntheticCheck> findByEnabled(Boolean enabled) {
        return jpaRepository.findByEnabled(enabled).stream()
                .map(this::toDomain)
                .toList();
    }
    
    @Override
    public void deleteById(SyntheticCheckId id) {
        jpaRepository.deleteById(id.getValue());
    }
    
    private SyntheticCheckEntity toEntity(SyntheticCheck check) {
        SyntheticCheckEntity entity = new SyntheticCheckEntity();
        entity.setId(check.getId().getValue());
        entity.setName(check.getName());
        entity.setDescription(check.getDescription());
        entity.setType(check.getType().name());
        entity.setEndpoint(check.getEndpoint());
        entity.setMethod(check.getMethod());
        entity.setHeaders(check.getHeaders());
        entity.setBody(check.getBody());
        entity.setExpectedStatusCode(check.getExpectedStatusCode());
        entity.setExpectedBodyPattern(check.getExpectedBodyPattern());
        entity.setTimeoutSeconds(check.getTimeoutSeconds());
        entity.setIntervalSeconds(check.getIntervalSeconds());
        entity.setEnabled(check.getEnabled());
        entity.setLastRunAt(check.getLastRunAt());
        if (check.getLastResult() != null) {
            SyntheticCheckResultEmbeddable resultEmb = new SyntheticCheckResultEmbeddable();
            resultEmb.setExecutedAt(check.getLastResult().getExecutedAt());
            resultEmb.setSuccess(check.getLastResult().getSuccess());
            resultEmb.setStatusCode(check.getLastResult().getStatusCode());
            resultEmb.setResponseTimeMs(check.getLastResult().getResponseTimeMs());
            resultEmb.setResponseBody(check.getLastResult().getResponseBody());
            resultEmb.setErrorMessage(check.getLastResult().getErrorMessage());
            resultEmb.setMetadata(check.getLastResult().getMetadata());
            entity.setLastResult(resultEmb);
        }
        entity.setLabels(check.getLabels());
        entity.setCreatedAt(check.getCreatedAt());
        entity.setUpdatedAt(check.getUpdatedAt());
        return entity;
    }
    
    private SyntheticCheck toDomain(SyntheticCheckEntity entity) {
        SyntheticCheck.SyntheticCheckResult lastResult = null;
        if (entity.getLastResult() != null) {
            SyntheticCheckResultEmbeddable resultEmb = entity.getLastResult();
            lastResult = SyntheticCheckResult.builder()
                    .executedAt(resultEmb.getExecutedAt())
                    .success(resultEmb.getSuccess())
                    .statusCode(resultEmb.getStatusCode())
                    .responseTimeMs(resultEmb.getResponseTimeMs())
                    .responseBody(resultEmb.getResponseBody())
                    .errorMessage(resultEmb.getErrorMessage())
                    .metadata(resultEmb.getMetadata())
                    .build();
        }
        
        return SyntheticCheck.builder()
                .id(new SyntheticCheckId(entity.getId()))
                .name(entity.getName())
                .description(entity.getDescription())
                .type(SyntheticCheck.SyntheticCheckType.valueOf(entity.getType()))
                .endpoint(entity.getEndpoint())
                .method(entity.getMethod())
                .headers(entity.getHeaders())
                .body(entity.getBody())
                .expectedStatusCode(entity.getExpectedStatusCode())
                .expectedBodyPattern(entity.getExpectedBodyPattern())
                .timeoutSeconds(entity.getTimeoutSeconds())
                .intervalSeconds(entity.getIntervalSeconds())
                .enabled(entity.getEnabled())
                .lastRunAt(entity.getLastRunAt())
                .lastResult(lastResult)
                .labels(entity.getLabels())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}

