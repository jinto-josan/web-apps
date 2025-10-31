package com.youtube.contentidservice.application.services;

import com.youtube.contentidservice.application.commands.CreateClaimCommand;
import com.youtube.contentidservice.application.commands.ResolveClaimCommand;
import com.youtube.contentidservice.application.dto.ClaimResponse;
import com.youtube.contentidservice.domain.entities.Claim;
import com.youtube.contentidservice.domain.entities.Match;
import com.youtube.contentidservice.domain.events.ClaimCreatedEvent;
import com.youtube.contentidservice.domain.events.ClaimResolvedEvent;
import com.youtube.contentidservice.domain.repositories.ClaimRepository;
import com.youtube.contentidservice.domain.repositories.EventPublisher;
import com.youtube.contentidservice.domain.repositories.MatchRepository;
import com.youtube.contentidservice.domain.valueobjects.ClaimId;
import com.youtube.contentidservice.domain.valueobjects.VideoId;
import com.youtube.contentidservice.application.mappers.ContentIdMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ClaimService {
    private final ClaimRepository claimRepository;
    private final MatchRepository matchRepository;
    private final EventPublisher eventPublisher;
    private final ContentIdMapper mapper;

    @Transactional
    public ClaimResponse createClaim(CreateClaimCommand command) {
        log.info("Creating claim for videoId: {} by owner: {}", 
                command.getClaimedVideoId(), command.getOwnerId());

        VideoId claimedVideoId = VideoId.of(command.getClaimedVideoId());

        // Fetch matches
        List<Match> matches = command.getMatchIds().stream()
                .map(matchId -> matchRepository.findById(matchId)
                        .orElseThrow(() -> new IllegalArgumentException("Match not found: " + matchId)))
                .collect(Collectors.toList());

        if (matches.isEmpty()) {
            throw new IllegalArgumentException("At least one match is required");
        }

        // Create claim
        Claim claim = Claim.create(claimedVideoId, command.getOwnerId(), matches);
        claimRepository.save(claim);

        // Publish event (via Service Bus for case workflow)
        List<UUID> matchIds = matches.stream()
                .map(Match::getId)
                .collect(Collectors.toList());
        
        eventPublisher.publish(new ClaimCreatedEvent(
                claim.getId(),
                claimedVideoId,
                command.getOwnerId(),
                matchIds
        ));

        log.info("Claim created: {}", claim.getId().getValue());
        return mapper.toResponse(claim);
    }

    @Transactional
    public ClaimResponse resolveClaim(ResolveClaimCommand command) {
        log.info("Resolving claim: {}", command.getClaimId());

        ClaimId claimId = ClaimId.of(command.getClaimId());
        Claim claim = claimRepository.findById(claimId)
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + command.getClaimId()));

        if (!claim.isActive()) {
            throw new IllegalStateException("Claim is not active: " + command.getClaimId());
        }

        claim.resolve(command.getResolution(), command.getDisputeStatus());
        claimRepository.save(claim);

        // Publish event (via Service Bus for case workflow)
        eventPublisher.publish(new ClaimResolvedEvent(
                claim.getId(),
                command.getDisputeStatus(),
                command.getResolution()
        ));

        return mapper.toResponse(claim);
    }

    public ClaimResponse getClaim(UUID claimId) {
        Claim claim = claimRepository.findById(ClaimId.of(claimId))
                .orElseThrow(() -> new IllegalArgumentException("Claim not found: " + claimId));
        return mapper.toResponse(claim);
    }

    public List<ClaimResponse> getClaimsByVideo(UUID videoId) {
        return mapper.toClaimResponseList(
                claimRepository.findByVideoId(VideoId.of(videoId))
        );
    }
}

