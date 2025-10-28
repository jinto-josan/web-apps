package com.youtube.drmservice.infrastructure.persistence.impl;

import com.youtube.drmservice.domain.models.AuditLog;
import com.youtube.drmservice.domain.repositories.AuditLogRepository;
import com.youtube.drmservice.infrastructure.persistence.entity.AuditLogEntity;
import com.youtube.drmservice.infrastructure.persistence.repository.AuditLogJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
@RequiredArgsConstructor
public class AuditLogRepositoryImpl implements AuditLogRepository {

    private final AuditLogJpaRepository jpaRepository;

    @Override
    public void save(AuditLog auditLog) {
        AuditLogEntity entity = toEntity(auditLog);
        jpaRepository.save(entity);
    }

    @Override
    public List<AuditLog> findByPolicyId(String policyId) {
        return jpaRepository.findByPolicyIdOrderByChangedAtDesc(policyId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private AuditLogEntity toEntity(AuditLog auditLog) {
        return AuditLogEntity.builder()
                .id(auditLog.getId())
                .policyId(auditLog.getPolicyId())
                .action(auditLog.getAction())
                .changedBy(auditLog.getChangedBy())
                .changedAt(auditLog.getChangedAt())
                .oldValues(auditLog.getOldValues())
                .newValues(auditLog.getNewValues())
                .correlationId(auditLog.getCorrelationId())
                .build();
    }

    private AuditLog toDomain(AuditLogEntity entity) {
        return AuditLog.builder()
                .id(entity.getId())
                .policyId(entity.getPolicyId())
                .action(entity.getAction())
                .changedBy(entity.getChangedBy())
                .changedAt(entity.getChangedAt())
                .oldValues(entity.getOldValues())
                .newValues(entity.getNewValues())
                .correlationId(entity.getCorrelationId())
                .build();
    }
}

