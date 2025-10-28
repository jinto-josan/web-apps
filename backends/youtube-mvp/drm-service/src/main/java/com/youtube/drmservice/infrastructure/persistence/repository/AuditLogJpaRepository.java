package com.youtube.drmservice.infrastructure.persistence.repository;

import com.youtube.drmservice.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AuditLogJpaRepository extends JpaRepository<AuditLogEntity, String> {
    List<AuditLogEntity> findByPolicyIdOrderByChangedAtDesc(String policyId);
}

