package com.youtube.livestreaming.infrastructure.persistence;

import com.youtube.livestreaming.domain.entities.LiveEvent;
import com.youtube.livestreaming.domain.ports.LiveEventRepository;
import com.youtube.livestreaming.infrastructure.persistence.entity.LiveEventEntity;
import com.youtube.livestreaming.infrastructure.persistence.repository.LiveEventJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class JpaLiveEventRepository implements LiveEventRepository {
    
    private final LiveEventJpaRepository jpaRepository;
    private final LiveEventEntityMapper mapper;
    
    @Override
    public LiveEvent save(LiveEvent liveEvent) {
        var entity = mapper.toEntity(liveEvent);
        var saved = jpaRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    @Override
    public Optional<LiveEvent> findById(String id) {
        return jpaRepository.findById(id)
            .map(mapper::toDomain);
    }
    
    @Override
    public Optional<LiveEvent> findByIdAndUserId(String id, String userId) {
        return jpaRepository.findByIdAndUserId(id, userId)
            .map(mapper::toDomain);
    }
    
    @Override
    public List<LiveEvent> findByChannelId(String channelId) {
        return jpaRepository.findByChannelId(channelId).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<LiveEvent> findByUserId(String userId) {
        return jpaRepository.findByUserId(userId).stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public List<LiveEvent> findAll() {
        return jpaRepository.findAll().stream()
            .map(mapper::toDomain)
            .collect(Collectors.toList());
    }
    
    @Override
    public void delete(String id) {
        jpaRepository.deleteById(id);
    }
    
    @Override
    public boolean exists(String id) {
        return jpaRepository.existsById(id);
    }
}

