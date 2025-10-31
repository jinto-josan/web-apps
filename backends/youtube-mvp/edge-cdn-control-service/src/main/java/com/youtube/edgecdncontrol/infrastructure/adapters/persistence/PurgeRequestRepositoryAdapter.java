package com.youtube.edgecdncontrol.infrastructure.adapters.persistence;

import com.youtube.edgecdncontrol.domain.entities.PurgeRequest;
import com.youtube.edgecdncontrol.domain.repositories.PurgeRequestRepository;
import com.youtube.edgecdncontrol.infrastructure.adapters.persistence.entity.PurgeRequestEntity;
import com.youtube.edgecdncontrol.infrastructure.adapters.persistence.jpa.PurgeRequestJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class PurgeRequestRepositoryAdapter implements PurgeRequestRepository {
    
    private final PurgeRequestJpaRepository jpaRepository;
    private final PurgeRequestEntityMapper mapper;
    
    @Override
    public PurgeRequest save(PurgeRequest request) {
        PurgeRequestEntity entity = mapper.toEntity(request);
        PurgeRequestEntity saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<PurgeRequest> findById(UUID id) {
        return jpaRepository.findById(id)
                .map(mapper::toDomain);
    }
    
    @Override
    public List<PurgeRequest> findByStatus(PurgeRequest.PurgeStatus status, int page, int size) {
        return jpaRepository.findByStatus(status, PageRequest.of(page, size))
                .stream()
                .map(mapper::toDomain)
                .collect(Collectors.toList());
    }
}

