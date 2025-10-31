package com.youtube.contentidservice.domain.repositories;

import com.youtube.contentidservice.domain.entities.Fingerprint;
import com.youtube.contentidservice.domain.valueobjects.FingerprintId;
import com.youtube.contentidservice.domain.valueobjects.VideoId;

import java.util.Optional;
import java.util.List;

public interface FingerprintRepository {
    void save(Fingerprint fingerprint);
    Optional<Fingerprint> findById(FingerprintId id);
    Optional<Fingerprint> findByVideoId(VideoId videoId);
    List<Fingerprint> findAllPending();
}

