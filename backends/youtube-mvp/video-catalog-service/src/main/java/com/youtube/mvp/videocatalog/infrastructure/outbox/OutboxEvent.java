package com.youtube.mvp.videocatalog.infrastructure.outbox;

import com.azure.spring.data.cosmos.core.mapping.Container;
import com.azure.spring.data.cosmos.core.mapping.PartitionKey;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.*;
import org.springframework.data.annotation.Id;

import java.time.Instant;

/**
 * Outbox event entity for reliable messaging.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Container(containerName = "outbox")
public class OutboxEvent {
    
    @Id
    private String eventId;
    
    @PartitionKey
    private String partitionKey; // partition by date or random
    
    private String aggregateType;
    private String aggregateId;
    private String eventType;
    private String payload;
    
    @JsonProperty("occurredAt")
    private Instant occurredAt;
    
    @JsonProperty("processedAt")
    private Instant processedAt;
    
    private String status; // PENDING, PROCESSED, FAILED
    private int retryCount;
}

