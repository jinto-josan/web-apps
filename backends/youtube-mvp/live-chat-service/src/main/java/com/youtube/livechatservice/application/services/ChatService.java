package com.youtube.livechatservice.application.services;

import com.youtube.livechatservice.domain.entities.ChatMessage;
import com.youtube.livechatservice.domain.repositories.ChatMessageRepository;
import com.youtube.livechatservice.domain.valueobjects.LiveId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChatService {
    private final ChatMessageRepository chatMessageRepository;

    public ChatMessage persist(ChatMessage message) {
        return chatMessageRepository.save(message);
    }

    public List<ChatMessage> getLatest(LiveId liveId, int limit) {
        return chatMessageRepository.findRecentByLiveId(liveId, limit);
    }

    public List<ChatMessage> getSince(LiveId liveId, Instant since, int limit) {
        return chatMessageRepository.findByLiveIdSince(liveId, since, limit);
    }
}


