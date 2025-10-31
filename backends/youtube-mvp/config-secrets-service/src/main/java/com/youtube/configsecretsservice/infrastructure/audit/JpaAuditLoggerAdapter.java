package com.youtube.configsecretsservice.infrastructure.audit;

import com.youtube.configsecretsservice.domain.entity.AuditLog;
import com.youtube.configsecretsservice.domain.port.AuditLoggerPort;
import com.youtube.configsecretsservice.infrastructure.persistence.entity.AuditLogEntity;
import com.youtube.configsecretsservice.infrastructure.persistence.repository.JpaAuditLogRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

/**
 * Adapter implementing AuditLoggerPort using JPA.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JpaAuditLoggerAdapter implements AuditLoggerPort {
    
    private final JpaAuditLogRepository auditLogRepository;
    
    @Override
    @Async
    public void log(AuditLog auditLog) {
        try {
            AuditLogEntity entity = AuditLogEntity.builder()
                    .scope(auditLog.getScope())
                    .key(auditLog.getKey())
                    .action(auditLog.getAction().name())
                    .userId(auditLog.getUserId())
                    .tenantId(auditLog.getTenantId())
                    .timestamp(auditLog.getTimestamp())
                    .details(auditLog.getDetails())
                    .ipAddress(auditLog.getIpAddress())
                    .userAgent(auditLog.getUserAgent())
                    .build();
            
            auditLogRepository.save(entity);
        } catch (Exception e) {
            log.error("Error logging audit entry: {}/{}", auditLog.getScope(), auditLog.getKey(), e);
        }
    }
}

