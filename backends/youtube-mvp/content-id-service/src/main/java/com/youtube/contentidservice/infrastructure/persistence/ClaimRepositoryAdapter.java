package com.youtube.contentidservice.infrastructure.persistence;

import com.youtube.contentidservice.domain.entities.Claim;
import com.youtube.contentidservice.domain.entities.Match;
import com.youtube.contentidservice.domain.repositories.ClaimRepository;
import com.youtube.contentidservice.domain.repositories.MatchRepository;
import com.youtube.contentidservice.domain.valueobjects.ClaimId;
import com.youtube.contentidservice.domain.valueobjects.DisputeStatus;
import com.youtube.contentidservice.domain.valueobjects.VideoId;
import com.youtube.contentidservice.infrastructure.persistence.entity.ClaimJpaEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class ClaimRepositoryAdapter implements ClaimRepository {
    private final JpaClaimRepository jpaRepository;
    private final MatchRepository matchRepository;

    @Override
    public void save(Claim claim) {
        jpaRepository.save(toEntity(claim));
    }

    @Override
    public Optional<Claim> findById(ClaimId id) {
        return jpaRepository.findById(id.getValue()).map(this::toDomain);
    }

    @Override
    public List<Claim> findByVideoId(VideoId videoId) {
        return jpaRepository.findByClaimedVideoId(videoId.getValue()).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Claim> findByOwnerId(UUID ownerId) {
        return jpaRepository.findByOwnerId(ownerId).stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Claim> findPending() {
        return jpaRepository.findByStatus("PENDING").stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    @Override
    public List<Claim> findUnderReview() {
        return jpaRepository.findByStatusAndDisputeStatus("REVIEWING", "UNDER_REVIEW").stream()
                .map(this::toDomain)
                .collect(Collectors.toList());
    }

    private ClaimJpaEntity toEntity(Claim claim) {
        List<UUID> matchIds = claim.getMatches().stream()
                .map(Match::getId)
                .collect(Collectors.toList());

        return ClaimJpaEntity.builder()
                .id(claim.getId().getValue())
                .claimedVideoId(claim.getClaimedVideoId().getValue())
                .ownerId(claim.getOwnerId())
                .matchIds(matchIds)
                .status(claim.getStatus())
                .disputeStatus(claim.getDisputeStatus().name())
                .createdAt(claim.getCreatedAt())
                .resolvedAt(claim.getResolvedAt())
                .resolution(claim.getResolution())
                .build();
    }

    private Claim toDomain(ClaimJpaEntity entity) {
        List<Match> matches = entity.getMatchIds().stream()
                .map(matchId -> matchRepository.findById(matchId)
                        .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId)))
                .collect(Collectors.toList());

        Claim claim = Claim.builder()
                .id(ClaimId.of(entity.getId()))
                .claimedVideoId(VideoId.of(entity.getClaimedVideoId()))
                .ownerId(entity.getOwnerId())
                .matches(matches)
                .status(entity.getStatus())
                .disputeStatus(DisputeStatus.valueOf(entity.getDisputeStatus()))
                .createdAt(entity.getCreatedAt())
                .resolvedAt(entity.getResolvedAt())
                .resolution(entity.getResolution())
                .build();

        return claim;
    }
}

