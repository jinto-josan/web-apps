package com.youtube.moderationservice.application.service;

import com.youtube.moderationservice.application.ports.ContentScannerPort;
import com.youtube.moderationservice.application.ports.ModerationCaseRepository;
import com.youtube.moderationservice.application.ports.PolicyRepository;
import com.youtube.moderationservice.application.ports.ServiceBusPort;
import com.youtube.moderationservice.domain.model.ModerationCase;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class ModerationApplicationServiceTest {

    @Test
    void createCase_autoReviewPublishesTask() {
        ContentScannerPort scanner = (c, ctx) -> Map.of("HATE_SPEECH", 0.95);
        ModerationCaseRepository repo = new InMemRepo();
        PolicyRepository policies = () -> List.of(new com.youtube.moderationservice.domain.model.PolicyThreshold("HATE_SPEECH", 0.9, 0.1));
        StringBuilder busPayload = new StringBuilder();
        ServiceBusPort bus = (caseId, payload) -> busPayload.append(payload);

        ModerationApplicationService svc = new ModerationApplicationService(scanner, repo, policies, bus);
        ModerationCase saved = svc.createCaseFromScores("content1", "user1", Map.of("HATE_SPEECH", 0.95));

        assertThat(saved.getStatus()).isEqualTo(ModerationCase.CaseStatus.UNDER_REVIEW);
        assertThat(busPayload).isNotEmpty();
    }

    static class InMemRepo implements ModerationCaseRepository {
        ModerationCase last;
        @Override public ModerationCase save(ModerationCase moderationCase) { last = moderationCase; return moderationCase; }
        @Override public Optional<ModerationCase> findById(UUID id) { return Optional.ofNullable(last); }
        @Override public java.util.List<ModerationCase> findByContentId(String contentId, int page, int size) { return java.util.List.of(); }
    }
}


