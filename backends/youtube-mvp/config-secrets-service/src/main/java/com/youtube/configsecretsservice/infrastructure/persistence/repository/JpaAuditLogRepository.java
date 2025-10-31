package com.youtube.configsecretsservice.infrastructure.persistence.repository;

import com.youtube.configsecretsservice.infrastructure.persistence.entity.AuditLogEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * JPA repository for audit log entries.
 */
@Repository
public interface JpaAuditLogRepository extends JpaRepository<AuditLogEntity, java.util.UUID> {
}

