package com.youtube.videouploadservice.infrastructure.persistence;

import jakarta.persistence.*;
import lombok.Data;

import java.time.Instant;

/**
 * JPA Entity for UploadQuota.
 */
@Data
@Entity
@Table(name = "upload_quota", indexes = {
    @Index(name = "idx_user_id_type", columnList = "user_id, quota_type", unique = true)
})
public class UploadQuotaEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "user_id", nullable = false)
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "quota_type", nullable = false)
    private QuotaType quotaType;
    
    @Column(name = "current_usage")
    private Long currentUsage;
    
    @Column(name = "quota_limit", nullable = false)
    private Long quotaLimit;
    
    @Column(name = "period_start")
    private Instant periodStart;
    
    @Column(name = "period_end", nullable = false)
    private Instant periodEnd;
    
    @Column(name = "upload_count")
    private Integer uploadCount;
    
    @Column(name = "upload_limit")
    private Integer uploadLimit;
    
    public enum QuotaType {
        DAILY,
        WEEKLY,
        MONTHLY,
        LIFETIME
    }
}

