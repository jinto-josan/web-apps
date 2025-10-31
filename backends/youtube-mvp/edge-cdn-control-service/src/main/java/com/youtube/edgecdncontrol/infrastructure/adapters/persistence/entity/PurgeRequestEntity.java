package com.youtube.edgecdncontrol.infrastructure.adapters.persistence.entity;

import com.youtube.edgecdncontrol.domain.entities.PurgeRequest;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "purge_requests", indexes = {
    @Index(name = "idx_status", columnList = "status"),
    @Index(name = "idx_requested_at", columnList = "requestedAt")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PurgeRequestEntity {
    @Id
    private UUID id;
    
    @Column(nullable = false)
    private String resourceGroup;
    
    @Column(nullable = false)
    private String profileName;
    
    @Column(columnDefinition = "TEXT")
    private String contentPaths; // JSON array
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurgeRequest.PurgeType purgeType;
    
    private String requestedBy;
    
    @Column(nullable = false)
    private Instant requestedAt;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PurgeRequest.PurgeStatus status;
    
    @Column(columnDefinition = "TEXT")
    private String errorMessage;
}

