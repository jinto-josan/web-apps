package com.youtube.identityauthservice.infrastructure.persistence;

import com.youtube.identityauthservice.domain.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, String> {
    List<OutboxEvent> findTop100ByDispatchedAtIsNullOrderByCreatedAtAsc();
    
    @Modifying
    @Query("UPDATE OutboxEvent o SET o.dispatchedAt = :dispatchedAt, o.brokerMessageId = :brokerMessageId, o.error= '' WHERE o.id = :id")
    void markDispatched(@Param("id") String id, @Param("brokerMessageId") String brokerMessageId, @Param("dispatchedAt") Instant dispatchedAt);

    @Modifying
    @Query("UPDATE OutboxEvent o SET o.error = :error WHERE o.id = :id")
    void markFailed(@Param("id") String id, @Param("error") String error);
}

