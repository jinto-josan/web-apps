package com.youtube.contentidservice.application.mappers;

import com.youtube.contentidservice.application.dto.ClaimResponse;
import com.youtube.contentidservice.application.dto.FingerprintResponse;
import com.youtube.contentidservice.application.dto.MatchResponse;
import com.youtube.contentidservice.domain.entities.Claim;
import com.youtube.contentidservice.domain.entities.Fingerprint;
import com.youtube.contentidservice.domain.entities.Match;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Mapper(componentModel = "spring")
public interface ContentIdMapper {
    ContentIdMapper INSTANCE = Mappers.getMapper(ContentIdMapper.class);

    @Mapping(source = "id.value", target = "id")
    @Mapping(source = "videoId.value", target = "videoId")
    @Mapping(source = "data.algorithm", target = "algorithm")
    @Mapping(source = "data.durationSeconds", target = "durationSeconds")
    @Mapping(source = "data.blobUri", target = "blobUri")
    FingerprintResponse toResponse(Fingerprint fingerprint);

    List<FingerprintResponse> toResponseList(List<Fingerprint> fingerprints);

    @Mapping(source = "id.value", target = "id")
    @Mapping(source = "claimedVideoId.value", target = "claimedVideoId")
    ClaimResponse toResponse(Claim claim);

    List<ClaimResponse> toClaimResponseList(List<Claim> claims);

    default MatchResponse toResponse(Match match) {
        if (match == null) {
            return null;
        }
        return MatchResponse.builder()
                .id(match.getId())
                .sourceFingerprintId(match.getSourceFingerprintId().getValue())
                .matchedFingerprintId(match.getMatchedFingerprintId().getValue())
                .sourceVideoId(match.getSourceVideoId().getValue())
                .matchedVideoId(match.getMatchedVideoId().getValue())
                .score(match.getScore().getValue())
                .detectedAt(match.getDetectedAt())
                .processed(match.isProcessed())
                .build();
    }

    default List<MatchResponse> toMatchResponseList(List<Match> matches) {
        if (matches == null) {
            return null;
        }
        return matches.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    default List<UUID> toMatchIdList(List<Match> matches) {
        if (matches == null) {
            return null;
        }
        return matches.stream()
                .map(Match::getId)
                .collect(Collectors.toList());
    }
}

