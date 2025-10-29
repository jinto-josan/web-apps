package com.youtube.moderationservice.infrastructure.web.controller;

import com.youtube.moderationservice.application.service.ModerationApplicationService;
import com.youtube.moderationservice.domain.model.ModerationCase;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/moderation")
@RequiredArgsConstructor
@Validated
public class ModerationController {
    private final ModerationApplicationService moderationService;

    @PostMapping("/scan")
    public ResponseEntity<Map<String, Double>> scan(@Valid @RequestBody ScanRequest request) {
        Map<String, Object> ctx = Map.of("contentType", request.getContentType());
        return ResponseEntity.ok(moderationService.scanContent(request.getContent(), ctx));
    }

    @PostMapping("/cases")
    public ResponseEntity<ModerationCase> createCase(@Valid @RequestBody CreateCaseRequest request) {
        ModerationCase saved = moderationService.createCaseFromScores(request.getContentId(), request.getReporterUserId(), request.getScores());
        return ResponseEntity.ok(saved);
    }

    @GetMapping("/cases")
    public ResponseEntity<?> listCases(@RequestParam("contentId") String contentId,
                                       @RequestParam(value = "page", defaultValue = "0") int page,
                                       @RequestParam(value = "size", defaultValue = "20") int size,
                                       @RequestHeader(value = HttpHeaders.IF_NONE_MATCH, required = false) String ifNoneMatch) {
        var cases = moderationService.listCases(contentId, page, size);
        String etag = "\"" + cases.stream().map(ModerationCase::getUpdatedAt).map(Object::toString).sorted().reduce("", String::concat).hashCode() + "\"";
        if (ifNoneMatch != null && ifNoneMatch.equals(etag)) {
            return ResponseEntity.status(304).eTag(etag).build();
        }
        return ResponseEntity.ok().eTag(etag).body(cases);
    }

    @PostMapping("/appeals")
    public ResponseEntity<Void> createAppeal(@Valid @RequestBody AppealRequest request) {
        // Stub: appeals processing would enqueue and update case state
        return ResponseEntity.accepted().build();
    }

    @Data
    public static class ScanRequest {
        @NotBlank private String content;
        @NotBlank private String contentType; // text, caption, comment
    }

    @Data
    public static class CreateCaseRequest {
        @NotBlank private String contentId;
        @NotBlank private String reporterUserId;
        private Map<String, Double> scores;
    }

    @Data
    public static class AppealRequest {
        @NotBlank private String caseId;
        @NotBlank private String reason;
    }
}


