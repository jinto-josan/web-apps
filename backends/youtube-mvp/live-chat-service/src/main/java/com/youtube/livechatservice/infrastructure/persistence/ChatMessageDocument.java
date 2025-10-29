package com.youtube.livechatservice.infrastructure.persistence;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import lombok.Data;
import org.springframework.data.annotation.Id;

import java.time.Instant;

@Data
@Container(containerName = "chat-messages")
public class ChatMessageDocument {
    @Id
    private String id;
    @PartitionKey
    private String liveId;
    private String userId;
    private String displayName;
    private String content;
    private Instant createdAt;
    private boolean moderated;
}


