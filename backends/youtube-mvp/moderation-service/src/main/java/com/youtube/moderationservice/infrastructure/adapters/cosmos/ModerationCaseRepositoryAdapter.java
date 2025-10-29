package com.youtube.moderationservice.infrastructure.adapters.cosmos;

import com.youtube.moderationservice.application.ports.ModerationCaseRepository;
import com.youtube.moderationservice.domain.model.ModerationCase;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.*;

@Component
@RequiredArgsConstructor
public class ModerationCaseRepositoryAdapter implements ModerationCaseRepository {
    private final ModerationCaseCosmosRepo repo;

    @Override
    public ModerationCase save(ModerationCase c) {
        ModerationCaseEntity e = new ModerationCaseEntity();
        e.setId(c.getId());
        e.setContentId(c.getContentId());
        e.setReporterUserId(c.getReporterUserId());
        e.setStatus(c.getStatus().name());
        e.setCreatedAt(c.getCreatedAt());
        e.setUpdatedAt(c.getUpdatedAt());
        repo.save(e);
        return c;
    }

    @Override
    public Optional<ModerationCase> findById(UUID id) {
        return repo.findById(id).map(this::toDomain);
    }

    @Override
    public List<ModerationCase> findByContentId(String contentId, int page, int size) {
        // For brevity: naive scan; production code should use query methods
        Iterable<ModerationCaseEntity> all = repo.findAll();
        List<ModerationCase> list = new ArrayList<>();
        for (ModerationCaseEntity e : all) {
            if (contentId.equals(e.getContentId())) list.add(toDomain(e));
        }
        return list.stream().skip((long) page * size).limit(size).toList();
    }

    private ModerationCase toDomain(ModerationCaseEntity e) {
        return ModerationCase.builder()
                .id(e.getId())
                .contentId(e.getContentId())
                .reporterUserId(e.getReporterUserId())
                .status(ModerationCase.CaseStatus.valueOf(e.getStatus()))
                .strikes(Collections.emptyList())
                .createdAt(e.getCreatedAt())
                .updatedAt(e.getUpdatedAt())
                .build();
    }
}


