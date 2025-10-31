package com.youtube.configsecretsservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * JPA entity for audit log entries.
 */
@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_scope_key", columnList = "scope,key"),
    @Index(name = "idx_user_id", columnList = "userId"),
    @Index(name = "idx_tenant_id", columnList = "tenantId"),
    @Index(name = "idx_timestamp", columnList = "timestamp")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    
    @Column(nullable = false)
    private String scope;
    
    @Column(nullable = false)
    private String key;
    
    @Column(nullable = false)
    private String action;
    
    @Column(name = "user_id")
    private String userId;
    
    @Column(name = "tenant_id")
    private String tenantId;
    
    @Column(nullable = false)
    private Instant timestamp;
    
    @Column(columnDefinition = "TEXT")
    private String details;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
}

