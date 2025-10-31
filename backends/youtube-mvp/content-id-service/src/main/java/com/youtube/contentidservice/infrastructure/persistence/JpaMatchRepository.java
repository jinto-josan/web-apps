package com.youtube.contentidservice.infrastructure.persistence;

import com.youtube.contentidservice.infrastructure.persistence.entity.MatchJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaMatchRepository extends JpaRepository<MatchJpaEntity, UUID> {
    List<MatchJpaEntity> findBySourceVideoId(UUID sourceVideoId);
    List<MatchJpaEntity> findByMatchedVideoId(UUID matchedVideoId);
    List<MatchJpaEntity> findByProcessedFalse();
    
    @Query("SELECT COUNT(m) > 0 FROM MatchJpaEntity m WHERE " +
           "m.sourceFingerprintId = :source AND m.matchedFingerprintId = :matched")
    boolean existsByFingerprintIds(@Param("source") UUID source, @Param("matched") UUID matched);
}

