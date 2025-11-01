package com.youtube.common.domain.events.inbox;

import com.youtube.common.domain.persistence.entity.InboxMessage;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;

/**
 * JPA implementation of InboxRepository.
 * Services should extend this class with their specific InboxMessage entity.
 */
@Repository
public abstract class JpaInboxRepository implements InboxRepository {
    
    protected final EntityManager entityManager;
    protected final Class<? extends InboxMessage> entityClass;
    
    protected JpaInboxRepository(EntityManager entityManager, Class<? extends InboxMessage> entityClass) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
    }
    
    @Override
    @Transactional
    public boolean beginProcess(String messageId) {
        // Try to insert - if it fails, message was already processed
        try {
            InboxMessage message = createInboxMessage();
            message.setMessageId(messageId);
            message.setFirstSeenAt(Instant.now());
            message.setAttempts(1);
            message.setLastAttemptAt(Instant.now());
            
            entityManager.persist(message);
            entityManager.flush();
            return true;
        } catch (Exception e) {
            // Message already exists - duplicate
            return false;
        }
    }
    
    @Override
    @Transactional
    public void markProcessed(String messageId) {
        InboxMessage message = entityManager.find(entityClass, messageId);
        if (message != null) {
            message.markProcessed();
            entityManager.merge(message);
        }
    }
    
    @Override
    @Transactional
    public void recordFailure(String messageId, String error) {
        InboxMessage message = entityManager.find(entityClass, messageId);
        if (message != null) {
            message.recordFailure(error);
            entityManager.merge(message);
        }
    }
    
    @Override
    public Optional<InboxMessage> findById(String messageId) {
        InboxMessage message = entityManager.find(entityClass, messageId);
        return Optional.ofNullable(message);
    }
    
    @Override
    public boolean isProcessed(String messageId) {
        Optional<InboxMessage> message = findById(messageId);
        return message.map(InboxMessage::isProcessed).orElse(false);
    }
    
    /**
     * Creates a new instance of the InboxMessage entity.
     * Subclasses should override this to return their specific entity type.
     */
    protected abstract InboxMessage createInboxMessage();
}

