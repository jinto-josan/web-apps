package com.youtube.channelservice.infrastructure.persistence.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import java.time.Instant;

/**
 * JPA entity for Channel Members.
 * Maps to the channel_members table in the database.
 */
@Entity
@Table(name = "channel_members",
       indexes = {
           @Index(name = "idx_channel_members_channel", columnList = "channelId"),
           @Index(name = "idx_channel_members_user", columnList = "userId"),
           @Index(name = "idx_channel_members_role", columnList = "role")
       },
       uniqueConstraints = {
           @UniqueConstraint(name = "uk_channel_members_channel_user", columnNames = {"channelId", "userId"})
       })
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChannelMemberEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(name = "channel_id", nullable = false, length = 26)
    @NotBlank(message = "Channel ID cannot be blank")
    private String channelId;
    
    @Column(name = "user_id", nullable = false, length = 26)
    @NotBlank(message = "User ID cannot be blank")
    private String userId;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false, length = 20)
    private Role role;
    
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;
    
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;
    
    @Version
    @Column(name = "version")
    private Long version; // For optimistic concurrency control
    
    @PrePersist
    protected void onCreate() {
        Instant now = Instant.now();
        if (createdAt == null) {
            createdAt = now;
        }
        if (updatedAt == null) {
            updatedAt = now;
        }
    }
    
    @PreUpdate
    protected void onUpdate() {
        updatedAt = Instant.now();
    }
}
