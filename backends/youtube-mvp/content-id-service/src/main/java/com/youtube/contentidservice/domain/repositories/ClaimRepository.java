package com.youtube.contentidservice.domain.repositories;

import com.youtube.contentidservice.domain.entities.Claim;
import com.youtube.contentidservice.domain.valueobjects.ClaimId;
import com.youtube.contentidservice.domain.valueobjects.VideoId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ClaimRepository {
    void save(Claim claim);
    Optional<Claim> findById(ClaimId id);
    List<Claim> findByVideoId(VideoId videoId);
    List<Claim> findByOwnerId(UUID ownerId);
    List<Claim> findPending();
    List<Claim> findUnderReview();
}

