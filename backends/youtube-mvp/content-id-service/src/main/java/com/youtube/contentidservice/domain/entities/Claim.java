package com.youtube.contentidservice.domain.entities;

import com.youtube.contentidservice.domain.valueobjects.ClaimId;
import com.youtube.contentidservice.domain.valueobjects.DisputeStatus;
import com.youtube.contentidservice.domain.valueobjects.VideoId;
import com.youtube.contentidservice.domain.entities.Match;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@Builder
public class Claim {
    private ClaimId id;
    private VideoId claimedVideoId;
    private UUID ownerId;
    private List<Match> matches;
    private String status; // PENDING, REVIEWING, RESOLVED, WITHDRAWN
    private DisputeStatus disputeStatus;
    private Instant createdAt;
    private Instant resolvedAt;
    private String resolution;

    public static Claim create(VideoId claimedVideoId, UUID ownerId, List<Match> matches) {
        if (matches == null || matches.isEmpty()) {
            throw new IllegalArgumentException("Claim must have at least one match");
        }

        return Claim.builder()
                .id(ClaimId.of(UUID.randomUUID()))
                .claimedVideoId(claimedVideoId)
                .ownerId(ownerId)
                .matches(new ArrayList<>(matches))
                .status("PENDING")
                .disputeStatus(DisputeStatus.PENDING)
                .createdAt(Instant.now())
                .build();
    }

    public void startReview() {
        this.status = "REVIEWING";
        this.disputeStatus = DisputeStatus.UNDER_REVIEW;
    }

    public void resolve(String resolution, DisputeStatus disputeStatus) {
        this.status = "RESOLVED";
        this.disputeStatus = disputeStatus;
        this.resolution = resolution;
        this.resolvedAt = Instant.now();
    }

    public void withdraw() {
        this.status = "WITHDRAWN";
        this.disputeStatus = DisputeStatus.WITHDRAWN;
        this.resolvedAt = Instant.now();
    }

    public boolean isActive() {
        return "PENDING".equals(status) || "REVIEWING".equals(status);
    }
}

