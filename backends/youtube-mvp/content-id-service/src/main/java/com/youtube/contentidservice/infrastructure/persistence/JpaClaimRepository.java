package com.youtube.contentidservice.infrastructure.persistence;

import com.youtube.contentidservice.infrastructure.persistence.entity.ClaimJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface JpaClaimRepository extends JpaRepository<ClaimJpaEntity, UUID> {
    List<ClaimJpaEntity> findByClaimedVideoId(UUID claimedVideoId);
    List<ClaimJpaEntity> findByOwnerId(UUID ownerId);
    List<ClaimJpaEntity> findByStatus(String status);
    List<ClaimJpaEntity> findByStatusAndDisputeStatus(String status, String disputeStatus);
}

