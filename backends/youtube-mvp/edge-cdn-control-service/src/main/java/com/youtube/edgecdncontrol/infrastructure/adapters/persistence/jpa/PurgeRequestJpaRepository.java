package com.youtube.edgecdncontrol.infrastructure.adapters.persistence.jpa;

import com.youtube.edgecdncontrol.domain.entities.PurgeRequest;
import com.youtube.edgecdncontrol.infrastructure.adapters.persistence.entity.PurgeRequestEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface PurgeRequestJpaRepository extends JpaRepository<PurgeRequestEntity, UUID> {
    Page<PurgeRequestEntity> findByStatus(PurgeRequest.PurgeStatus status, Pageable pageable);
}

