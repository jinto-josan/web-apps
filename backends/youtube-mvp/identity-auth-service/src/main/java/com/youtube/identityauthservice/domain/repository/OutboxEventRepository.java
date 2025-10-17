package com.youtube.identityauthservice.domain.repository;


import com.youtube.identityauthservice.domain.model.OutboxEvent;

import java.time.Instant;
import java.util.List;

public interface OutboxEventRepository {
    // Should lock rows to avoid double send; use SELECT … FOR UPDATE SKIP LOCKED in Postgres
    List<OutboxEvent> findNextPendingBatch(int limit);

    void markDispatched(String id, String brokerMessageId, Instant dispatchedAt);

    void markFailed(String id, String error);
}
