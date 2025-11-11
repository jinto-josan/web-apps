package com.youtube.userprofileservice.infrastructure.persistence;

import com.youtube.common.domain.events.inbox.JpaInboxRepository;
import com.youtube.common.domain.persistence.entity.InboxMessage;
import com.youtube.userprofileservice.infrastructure.persistence.entity.InboxMessageEntity;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

/**
 * JPA implementation of InboxRepository for user-profile-service.
 * Provides inbox idempotency for event processing.
 */
@Repository
public class InboxRepositoryImpl extends JpaInboxRepository {
    
    public InboxRepositoryImpl(EntityManager entityManager) {
        super(entityManager, InboxMessageEntity.class);
    }
    
    @Override
    protected InboxMessage createInboxMessage() {
        return new InboxMessageEntity();
    }
}

