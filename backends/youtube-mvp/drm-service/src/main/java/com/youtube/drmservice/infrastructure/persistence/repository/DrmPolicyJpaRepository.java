package com.youtube.drmservice.infrastructure.persistence.repository;

import com.youtube.drmservice.infrastructure.persistence.entity.DrmPolicyEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DrmPolicyJpaRepository extends JpaRepository<DrmPolicyEntity, String> {
    Optional<DrmPolicyEntity> findByVideoId(String videoId);
    boolean existsByVideoId(String videoId);
}

