package com.youtube.contentidservice.domain.repositories;

import com.youtube.contentidservice.domain.entities.Match;
import com.youtube.contentidservice.domain.valueobjects.FingerprintId;
import com.youtube.contentidservice.domain.valueobjects.VideoId;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MatchRepository {
    void save(Match match);
    Optional<Match> findById(UUID id);
    List<Match> findBySourceVideoId(VideoId videoId);
    List<Match> findByMatchedVideoId(VideoId videoId);
    List<Match> findUnprocessed();
    boolean existsByFingerprintIds(FingerprintId source, FingerprintId matched);
}

