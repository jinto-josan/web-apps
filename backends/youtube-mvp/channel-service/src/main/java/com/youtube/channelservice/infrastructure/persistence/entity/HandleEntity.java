package com.youtube.channelservice.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.Instant;

/**
 * JPA entity for Handle Registry.
 * Maps to the handles table in the database.
 */
@Entity
@Table(name = "handles",
       indexes = {
           @Index(name = "idx_handles_bucket", columnList = "bucket"),
           @Index(name = "idx_handles_status", columnList = "status"),
           @Index(name = "idx_handles_reserved_at", columnList = "reservedAt")
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HandleEntity {
    
    @Id
    @Column(name = "id", length = 30)
    @NotBlank(message = "Handle ID cannot be blank")
    private String id; // handleLower
    
    @Column(name = "bucket", nullable = false)
    private int bucket; // Partition key for distribution
    
    @Column(name = "channel_id", length = 26)
    private String channelId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private HandleStatus status;
    
    @Column(name = "reserved_by_user_id", length = 26)
    private String reservedByUserId;
    
    @Column(name = "reserved_at")
    private Instant reservedAt;
    
    @Column(name = "committed_at")
    private Instant committedAt;
    
    @Column(name = "ttl_seconds")
    private Integer ttlSeconds; // TTL in seconds
    
    @Version
    @Column(name = "version")
    private Long version; // For optimistic concurrency control
    
    public enum HandleStatus {
        RESERVED, COMMITTED, RELEASED
    }
}
