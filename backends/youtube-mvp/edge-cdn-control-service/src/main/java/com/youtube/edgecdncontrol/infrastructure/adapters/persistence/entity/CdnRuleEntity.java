package com.youtube.edgecdncontrol.infrastructure.adapters.persistence.entity;

import com.youtube.edgecdncontrol.domain.valueobjects.RuleStatus;
import com.youtube.edgecdncontrol.domain.valueobjects.RuleType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;

@Entity
@Table(name = "cdn_rules", indexes = {
    @Index(name = "idx_resource_group_profile", columnList = "resourceGroup,profileName"),
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_created_at", columnList = "createdAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CdnRuleEntity {
    @Id
    private String id;
    
    @Column(nullable = false)
    private String name;
    
    private String description;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleType ruleType;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RuleStatus status;
    
    @Column(nullable = false)
    private String resourceGroup;
    
    @Column(nullable = false)
    private String profileName;
    
    private Integer priority;
    
    @Column(columnDefinition = "TEXT")
    private String matchConditions; // JSON
    
    @Column(columnDefinition = "TEXT")
    private String action; // JSON
    
    @Column(columnDefinition = "TEXT")
    private String metadata; // JSON
    
    private String createdBy;
    
    @Column(nullable = false)
    private Instant createdAt;
    
    @Column(nullable = false)
    private Instant updatedAt;
    
    @Version
    private String version; // ETag for optimistic locking
    
    private String rollbackFromRuleId;
}

