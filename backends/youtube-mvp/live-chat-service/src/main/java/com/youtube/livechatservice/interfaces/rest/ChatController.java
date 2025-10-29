package com.youtube.livechatservice.interfaces.rest;

import com.youtube.livechatservice.application.services.ChatService;
import com.youtube.livechatservice.domain.entities.ChatMessage;
import com.youtube.livechatservice.domain.valueobjects.LiveId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/v1/live/{id}/chat")
@Validated
@RequiredArgsConstructor
public class ChatController {

    private final ChatService chatService;
    private final com.youtube.livechatservice.infrastructure.external.WebPubSubAdapter webPubSubAdapter;

    @PostMapping("/messages")
    public ResponseEntity<MessageResponse> postMessage(
            @PathVariable("id") String liveId,
            @RequestHeader(value = "Idempotency-Key", required = false) String idempotencyKey,
            @Valid @RequestBody SendMessageRequest request
    ) {
        ChatMessage message = ChatMessage.builder()
                .messageId(request.getClientMessageId())
                .liveId(LiveId.of(liveId))
                .userId(request.getUserId())
                .displayName(request.getDisplayName())
                .content(request.getContent())
                .createdAt(Instant.now())
                .moderated(false)
                .build();
        ChatMessage saved = chatService.persist(message);
        HttpHeaders headers = new HttpHeaders();
        headers.setETag('"' + saved.getMessageId() + '"');
        return new ResponseEntity<>(MessageResponse.from(saved), headers, HttpStatus.CREATED);
    }

    @GetMapping("/history")
    public ResponseEntity<List<MessageResponse>> history(
            @PathVariable("id") String liveId,
            @RequestParam(value = "since", required = false) Instant since,
            @RequestParam(value = "limit", defaultValue = "50") @Min(1) @Max(200) int limit
    ) {
        List<ChatMessage> messages = since == null
                ? chatService.getLatest(LiveId.of(liveId), limit)
                : chatService.getSince(LiveId.of(liveId), since, limit);
        return ResponseEntity.ok(messages.stream().map(MessageResponse::from).toList());
    }

    @PostMapping("/token")
    public ResponseEntity<TokenResponse> issueToken(@PathVariable("id") String liveId,
                                                    @Valid @RequestBody TokenRequest request) {
        var token = webPubSubAdapter.issueClientToken(request.getUserId(), java.time.Duration.ofHours(1));
        return ResponseEntity.ok(TokenResponse.builder().token(token.token()).expiresAt(token.expiresAt()).build());
    }

    @Data
    public static class SendMessageRequest {
        @NotBlank
        private String clientMessageId;
        @NotBlank
        private String userId;
        @NotBlank
        private String displayName;
        @NotBlank
        private String content;
    }

    @Data
    @Builder
    public static class MessageResponse {
        private String messageId;
        private String userId;
        private String displayName;
        private String content;
        private Instant createdAt;

        public static MessageResponse from(ChatMessage message) {
            return MessageResponse.builder()
                    .messageId(message.getMessageId())
                    .userId(message.getUserId())
                    .displayName(message.getDisplayName())
                    .content(message.getContent())
                    .createdAt(message.getCreatedAt())
                    .build();
        }
    }

    @Data
    public static class TokenRequest {
        @NotBlank
        private String userId;
        @NotBlank
        private String role; // viewer/moderator
    }

    @Data
    @Builder
    public static class TokenResponse {
        private String token;
        private Instant expiresAt;
    }
}


