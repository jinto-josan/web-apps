package com.youtube.contentidservice.infrastructure.persistence.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "claims", indexes = {
    @Index(name = "idx_claimed_video", columnList = "claimed_video_id"),
    @Index(name = "idx_owner", columnList = "owner_id"),
    @Index(name = "idx_status", columnList = "status")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClaimJpaEntity {
    @Id
    @Column(columnDefinition = "UUID")
    private UUID id;

    @Column(name = "claimed_video_id", nullable = false, columnDefinition = "UUID")
    private UUID claimedVideoId;

    @Column(name = "owner_id", nullable = false, columnDefinition = "UUID")
    private UUID ownerId;

    @ElementCollection
    @CollectionTable(name = "claim_matches", joinColumns = @JoinColumn(name = "claim_id"))
    @Column(name = "match_id", columnDefinition = "UUID")
    private List<UUID> matchIds;

    @Column(name = "status", nullable = false)
    private String status;

    @Column(name = "dispute_status", nullable = false)
    private String disputeStatus;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "resolved_at")
    private Instant resolvedAt;

    @Column(name = "resolution", length = 1000)
    private String resolution;
}

