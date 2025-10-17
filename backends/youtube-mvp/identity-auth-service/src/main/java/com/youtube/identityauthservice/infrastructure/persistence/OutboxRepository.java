package com.youtube.identityauthservice.infrastructure.persistence;

import com.youtube.identityauthservice.domain.model.OutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OutboxRepository extends JpaRepository<OutboxEvent, String> {
    List<OutboxEvent> findTop100ByDispatchedAtIsNullOrderByCreatedAtAsc();
}

