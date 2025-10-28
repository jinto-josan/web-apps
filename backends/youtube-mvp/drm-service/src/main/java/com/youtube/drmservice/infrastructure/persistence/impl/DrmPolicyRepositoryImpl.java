package com.youtube.drmservice.infrastructure.persistence.impl;

import com.youtube.drmservice.domain.models.DrmPolicy;
import com.youtube.drmservice.domain.repositories.DrmPolicyRepository;
import com.youtube.drmservice.infrastructure.persistence.entity.DrmPolicyEntity;
import com.youtube.drmservice.infrastructure.persistence.mapper.DrmPolicyMapper;
import com.youtube.drmservice.infrastructure.persistence.repository.DrmPolicyJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class DrmPolicyRepositoryImpl implements DrmPolicyRepository {

    private final DrmPolicyJpaRepository jpaRepository;
    private static final DrmPolicyMapper mapper = DrmPolicyMapper.INSTANCE;

    @Override
    public Optional<DrmPolicy> findById(String id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }

    @Override
    public Optional<DrmPolicy> findByVideoId(String videoId) {
        return jpaRepository.findByVideoId(videoId)
                .map(mapper::toDomain);
    }

    @Override
    public DrmPolicy save(DrmPolicy policy) {
        DrmPolicyEntity entity = mapper.toEntity(policy);
        DrmPolicyEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }

    @Override
    public void delete(String id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public boolean existsByVideoId(String videoId) {
        return jpaRepository.existsByVideoId(videoId);
    }
}

