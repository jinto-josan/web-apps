package com.youtube.userprofileservice.infrastructure.persistence;

import com.youtube.userprofileservice.infrastructure.persistence.entity.OutboxEventEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

/**
 * Spring Data JPA repository for OutboxEventEntity.
 * Provides methods for querying and updating outbox events.
 */
@Repository
public interface OutboxRepository extends JpaRepository<OutboxEventEntity, String> {
    
    /**
     * Finds the top 100 undelivered outbox events, ordered by creation time (oldest first).
     * Used by the outbox dispatcher to process events in order.
     * 
     * @return list of undelivered outbox events
     */
    List<OutboxEventEntity> findTop100ByDispatchedAtIsNullOrderByCreatedAtAsc();
    
    /**
     * Marks an outbox event as successfully dispatched.
     * 
     * @param id the event ID
     * @param brokerMessageId the message ID assigned by the message broker
     * @param dispatchedAt the timestamp when the event was dispatched
     */
    @Modifying
    @Query("UPDATE OutboxEventEntity o SET o.dispatchedAt = :dispatchedAt, o.brokerMessageId = :brokerMessageId, o.error = '' WHERE o.id = :id")
    void markDispatched(@Param("id") String id, @Param("brokerMessageId") String brokerMessageId, @Param("dispatchedAt") Instant dispatchedAt);

    /**
     * Marks an outbox event as failed with an error message.
     * 
     * @param id the event ID
     * @param error the error message
     */
    @Modifying
    @Query("UPDATE OutboxEventEntity o SET o.error = :error WHERE o.id = :id")
    void markFailed(@Param("id") String id, @Param("error") String error);
}

