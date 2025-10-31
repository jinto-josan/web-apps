package com.youtube.contentidservice.infrastructure.persistence;

import com.youtube.contentidservice.infrastructure.persistence.entity.FingerprintJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaFingerprintRepository extends JpaRepository<FingerprintJpaEntity, UUID> {
    Optional<FingerprintJpaEntity> findByVideoId(UUID videoId);
    List<FingerprintJpaEntity> findByStatus(String status);
}

