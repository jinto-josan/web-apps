package com.youtube.moderationservice.application.ports;

import com.youtube.moderationservice.domain.model.ModerationCase;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ModerationCaseRepository {
    ModerationCase save(ModerationCase moderationCase);
    Optional<ModerationCase> findById(UUID id);
    List<ModerationCase> findByContentId(String contentId, int page, int size);
}


