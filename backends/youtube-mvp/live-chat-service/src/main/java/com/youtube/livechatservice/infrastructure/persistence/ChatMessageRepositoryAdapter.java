package com.youtube.livechatservice.infrastructure.persistence;

import com.youtube.livechatservice.domain.entities.ChatMessage;
import com.youtube.livechatservice.domain.repositories.ChatMessageRepository;
import com.youtube.livechatservice.domain.valueobjects.LiveId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class ChatMessageRepositoryAdapter implements ChatMessageRepository {

    private final ChatMessageCosmosRepository cosmosRepository;

    @Override
    public ChatMessage save(ChatMessage message) {
        ChatMessageDocument doc = new ChatMessageDocument();
        doc.setId(message.getMessageId());
        doc.setLiveId(message.getLiveId().getValue());
        doc.setUserId(message.getUserId());
        doc.setDisplayName(message.getDisplayName());
        doc.setContent(message.getContent());
        doc.setCreatedAt(message.getCreatedAt());
        doc.setModerated(message.isModerated());
        ChatMessageDocument saved = cosmosRepository.save(doc);
        return toDomain(saved);
    }

    @Override
    public List<ChatMessage> findRecentByLiveId(LiveId liveId, int limit) {
        return cosmosRepository.findTop200ByLiveIdOrderByCreatedAtDesc(liveId.getValue())
                .stream().limit(limit).map(this::toDomain).toList();
    }

    @Override
    public List<ChatMessage> findByLiveIdSince(LiveId liveId, Instant since, int limit) {
        return cosmosRepository.findTop200ByLiveIdAndCreatedAtAfterOrderByCreatedAtAsc(liveId.getValue(), since)
                .stream().limit(limit).map(this::toDomain).toList();
    }

    @Override
    public Optional<ChatMessage> findById(String messageId) {
        return cosmosRepository.findById(messageId).map(this::toDomain);
    }

    private ChatMessage toDomain(ChatMessageDocument doc) {
        return ChatMessage.builder()
                .messageId(doc.getId())
                .liveId(LiveId.of(doc.getLiveId()))
                .userId(doc.getUserId())
                .displayName(doc.getDisplayName())
                .content(doc.getContent())
                .createdAt(doc.getCreatedAt())
                .moderated(doc.isModerated())
                .build();
    }
}


