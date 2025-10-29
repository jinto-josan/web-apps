package com.youtube.livechatservice.infrastructure.messaging;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ModerationEvent {
    private String messageId;
    private String liveId;
    private String action; // remove/timeout
}


