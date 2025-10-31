package com.youtube.edgecdncontrol.domain.repositories;

import com.youtube.edgecdncontrol.domain.entities.PurgeRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PurgeRequestRepository {
    PurgeRequest save(PurgeRequest request);
    Optional<PurgeRequest> findById(UUID id);
    List<PurgeRequest> findByStatus(PurgeRequest.PurgeStatus status, int page, int size);
}

