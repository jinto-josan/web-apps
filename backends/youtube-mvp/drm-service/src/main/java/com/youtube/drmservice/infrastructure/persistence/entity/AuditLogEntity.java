package com.youtube.drmservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "audit_logs", indexes = {
    @Index(name = "idx_policy_id", columnList = "policy_id"),
    @Index(name = "idx_changed_at", columnList = "changed_at")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogEntity {

    @Id
    private String id;

    @Column(name = "policy_id", nullable = false)
    private String policyId;

    @Column(name = "action", nullable = false)
    private String action;

    @Column(name = "changed_by", nullable = false)
    private String changedBy;

    @Column(name = "changed_at", nullable = false)
    private Instant changedAt;

    @ElementCollection
    @CollectionTable(name = "audit_old_values", joinColumns = @JoinColumn(name = "audit_id"))
    @MapKeyColumn(name = "field_name")
    @Column(name = "field_value")
    private Map<String, String> oldValues;

    @ElementCollection
    @CollectionTable(name = "audit_new_values", joinColumns = @JoinColumn(name = "audit_id"))
    @MapKeyColumn(name = "field_name")
    @Column(name = "field_value")
    private Map<String, String> newValues;

    @Column(name = "correlation_id")
    private String correlationId;
}

