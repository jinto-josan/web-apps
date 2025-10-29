package com.youtube.livechatservice.domain.repositories;

import com.youtube.livechatservice.domain.entities.ChatMessage;
import com.youtube.livechatservice.domain.valueobjects.LiveId;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface ChatMessageRepository {
    ChatMessage save(ChatMessage message);
    List<ChatMessage> findRecentByLiveId(LiveId liveId, int limit);
    List<ChatMessage> findByLiveIdSince(LiveId liveId, Instant since, int limit);
    Optional<ChatMessage> findById(String messageId);
}


