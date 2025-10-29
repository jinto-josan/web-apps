package com.youtube.moderationservice.application.service;

import com.youtube.moderationservice.application.ports.ContentScannerPort;
import com.youtube.moderationservice.application.ports.ModerationCaseRepository;
import com.youtube.moderationservice.application.ports.PolicyRepository;
import com.youtube.moderationservice.application.ports.ServiceBusPort;
import com.youtube.moderationservice.domain.model.PolicyThreshold;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

public class PolicyLogicTest {
    @Test
    void belowThreshold_closesCase() {
        ContentScannerPort scanner = (c, ctx) -> Map.of("HATE_SPEECH", 0.05);
        ModerationCaseRepository repo = new ModerationApplicationServiceTest.InMemRepo();
        PolicyRepository policies = () -> List.of(new PolicyThreshold("HATE_SPEECH", 0.9, 0.1));
        ServiceBusPort bus = (caseId, payload) -> {};

        ModerationApplicationService svc = new ModerationApplicationService(scanner, repo, policies, bus);
        var mc = svc.createCaseFromScores("content2", "user2", Map.of("HATE_SPEECH", 0.05));
        assertThat(mc.getStatus().name()).isEqualTo("CLOSED");
    }
}


