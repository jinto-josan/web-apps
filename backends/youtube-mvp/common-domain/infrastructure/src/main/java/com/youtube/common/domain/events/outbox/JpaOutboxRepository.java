package com.youtube.common.domain.events.outbox;

import com.github.f4b6a3.ulid.UlidCreator;
import com.youtube.common.domain.persistence.entity.OutboxEvent;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

/**
 * JPA implementation of OutboxRepository.
 * Services should extend this class with their specific OutboxEvent entity.
 */
public abstract class JpaOutboxRepository implements OutboxRepository {
    
    protected final EntityManager entityManager;
    protected final Class<? extends OutboxEvent> entityClass;
    
    protected JpaOutboxRepository(EntityManager entityManager, Class<? extends OutboxEvent> entityClass) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
    }
    
    @Override
    @Transactional
    public void append(List<OutboxEventData> events, String correlationId, String causationId, String traceparent) {
        Instant now = Instant.now();
        
        for (OutboxEventData eventData : events) {
            OutboxEvent event = createOutboxEvent();
            event.setId(UlidCreator.getUlid().toString());
            event.setEventType(eventData.eventType());
            event.setAggregateType(eventData.aggregateType());
            event.setAggregateId(eventData.aggregateId());
            event.setPayloadJson(eventData.payloadJson());
            event.setCorrelationId(correlationId);
            event.setCausationId(causationId);
            event.setTraceparent(traceparent);
            event.setCreatedAt(now);
            
            entityManager.persist(event);
        }
    }
    
    @Override
    @Transactional
    public List<OutboxEvent> fetchPendingBatch(int limit) {
        // Use SELECT ... FOR UPDATE SKIP LOCKED for concurrent processing
        String jpql = "SELECT e FROM " + entityClass.getSimpleName() + 
                     " e WHERE e.dispatchedAt IS NULL ORDER BY e.createdAt ASC";
        
        Query query = entityManager.createQuery(jpql, entityClass)
            .setMaxResults(limit)
            .setLockMode(LockModeType.PESSIMISTIC_WRITE);
        
        @SuppressWarnings("unchecked")
        List<OutboxEvent> result = query.getResultList();
        return result;
    }
    
    @Override
    @Transactional
    public void markDispatched(String eventId, String brokerMessageId) {
        OutboxEvent event = entityManager.find(entityClass, eventId);
        if (event != null) {
            event.setDispatchedAt(Instant.now());
            event.setBrokerMessageId(brokerMessageId);
            entityManager.merge(event);
        }
    }
    
    @Override
    @Transactional
    public void markFailed(String eventId, String error) {
        OutboxEvent event = entityManager.find(entityClass, eventId);
        if (event != null) {
            event.setError(error != null && error.length() > 4000 ? error.substring(0, 4000) : error);
            entityManager.merge(event);
        }
    }
    
    @Override
    public Optional<OutboxEvent> findById(String eventId) {
        OutboxEvent event = entityManager.find(entityClass, eventId);
        return Optional.ofNullable(event);
    }
    
    /**
     * Creates a new instance of the OutboxEvent entity.
     * Subclasses should override this to return their specific entity type.
     */
    protected abstract OutboxEvent createOutboxEvent();
}

