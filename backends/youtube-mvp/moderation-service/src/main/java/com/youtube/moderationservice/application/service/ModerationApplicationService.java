package com.youtube.moderationservice.application.service;

import com.youtube.moderationservice.application.ports.ContentScannerPort;
import com.youtube.moderationservice.application.ports.ModerationCaseRepository;
import com.youtube.moderationservice.application.ports.PolicyRepository;
import com.youtube.moderationservice.application.ports.ServiceBusPort;
import com.youtube.moderationservice.domain.model.ModerationCase;
import com.youtube.moderationservice.domain.model.PolicyThreshold;
import com.youtube.moderationservice.domain.model.Strike;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;

@Service
@RequiredArgsConstructor
public class ModerationApplicationService {
    private final ContentScannerPort contentScannerPort;
    private final ModerationCaseRepository moderationCaseRepository;
    private final PolicyRepository policyRepository;
    private final ServiceBusPort serviceBusPort;

    public Map<String, Double> scanContent(String content, Map<String, Object> context) {
        return contentScannerPort.scanText(content, context);
    }

    public ModerationCase createCaseFromScores(String contentId, String reporterUserId, Map<String, Double> scores) {
        List<PolicyThreshold> thresholds = policyRepository.findAll();
        boolean requiresReview = thresholds.stream().anyMatch(t -> scores.getOrDefault(t.getPolicyCode(), 0.0) >= t.getAutoRejectScore());

        ModerationCase.CaseStatus status = requiresReview ? ModerationCase.CaseStatus.UNDER_REVIEW : ModerationCase.CaseStatus.CLOSED;

        ModerationCase mc = ModerationCase.builder()
                .id(UUID.randomUUID())
                .contentId(contentId)
                .reporterUserId(reporterUserId)
                .status(status)
                .strikes(Collections.emptyList())
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        ModerationCase saved = moderationCaseRepository.save(mc);
        if (requiresReview) {
            serviceBusPort.publishReviewTask(saved.getId().toString(), "{\"contentId\":\"" + contentId + "\"}");
        }
        return saved;
    }

    public ModerationCase addStrike(UUID caseId, Strike strike) {
        ModerationCase existing = moderationCaseRepository.findById(caseId).orElseThrow();
        List<Strike> newStrikes = new ArrayList<>(existing.getStrikes());
        newStrikes.add(strike);
        ModerationCase updated = ModerationCase.builder()
                .id(existing.getId())
                .contentId(existing.getContentId())
                .reporterUserId(existing.getReporterUserId())
                .status(existing.getStatus())
                .strikes(List.copyOf(newStrikes))
                .createdAt(existing.getCreatedAt())
                .updatedAt(Instant.now())
                .build();
        return moderationCaseRepository.save(updated);
    }

    public List<ModerationCase> listCases(String contentId, int page, int size) {
        return moderationCaseRepository.findByContentId(contentId, page, size);
    }
}


