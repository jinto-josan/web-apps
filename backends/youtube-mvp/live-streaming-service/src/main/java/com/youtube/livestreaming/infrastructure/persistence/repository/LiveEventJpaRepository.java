package com.youtube.livestreaming.infrastructure.persistence.repository;

import com.youtube.livestreaming.infrastructure.persistence.entity.LiveEventEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface LiveEventJpaRepository extends JpaRepository<LiveEventEntity, String> {
    
    Optional<LiveEventEntity> findByIdAndUserId(String id, String userId);
    
    List<LiveEventEntity> findByChannelId(String channelId);
    
    List<LiveEventEntity> findByUserId(String userId);
    
    Page<LiveEventEntity> findByUserId(String userId, Pageable pageable);
    
    List<LiveEventEntity> findByUserIdAndStateIn(String userId, List<String> states);
}

