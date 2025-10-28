package com.youtube.mvp.videocatalog.infrastructure.outbox;

import org.springframework.stereotype.Repository;

/**
 * Outbox repository interface.
 */
@Repository
public interface OutboxRepository extends org.springframework.data.repository.CrudRepository<OutboxEvent, String> {
}

