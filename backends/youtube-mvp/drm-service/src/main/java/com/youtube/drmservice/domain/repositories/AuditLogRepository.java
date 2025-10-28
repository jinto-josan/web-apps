package com.youtube.drmservice.domain.repositories;

import com.youtube.drmservice.domain.models.AuditLog;
import java.util.List;

/**
 * Port for audit log persistence
 */
public interface AuditLogRepository {
    void save(AuditLog auditLog);
    List<AuditLog> findByPolicyId(String policyId);
}

