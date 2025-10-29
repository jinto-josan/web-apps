package com.youtube.livechatservice.domain.entities;

import com.youtube.livechatservice.domain.valueobjects.LiveId;
import lombok.Builder;
import lombok.Data;

import java.time.Instant;

@Data
@Builder
public class ChatMessage {
    private String messageId;
    private LiveId liveId;
    private String userId;
    private String displayName;
    private String content;
    private Instant createdAt;
    private boolean moderated;
}


