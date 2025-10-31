package com.youtube.configsecretsservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

/**
 * JPA entity for configuration entries.
 */
@Entity
@Table(name = "configuration_entries", indexes = {
    @Index(name = "idx_scope_key", columnList = "scope,key")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConfigurationEntryEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private String id;
    
    @Column(nullable = false)
    private String scope;
    
    @Column(nullable = false)
    private String key;
    
    @Column(columnDefinition = "TEXT")
    private String value;
    
    @Column(name = "content_type")
    private String contentType;
    
    private String label;
    
    private String etag;
    
    @Column(name = "is_secret")
    private Boolean isSecret;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Column(name = "created_by")
    private String createdBy;
    
    @Column(name = "updated_by")
    private String updatedBy;
}

