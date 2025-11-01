package com.youtube.common.domain.persistence.repository;

import com.youtube.common.domain.core.AggregateRoot;
import com.youtube.common.domain.core.Identifier;
import com.youtube.common.domain.core.ConcurrencyException;
import com.youtube.common.domain.repository.Repository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.LockModeType;
import jakarta.persistence.Query;

import java.util.Optional;

/**
 * Base JPA implementation of the Repository interface.
 * Provides optimistic concurrency control using version fields.
 * 
 * @param <T> the aggregate root type
 * @param <ID> the identifier type
 */
public abstract class JpaRepositoryBase<T extends AggregateRoot<ID>, ID extends Identifier<?>> 
        implements Repository<T, ID> {
    
    protected final EntityManager entityManager;
    protected final Class<T> entityClass;
    
    protected JpaRepositoryBase(EntityManager entityManager, Class<T> entityClass) {
        this.entityManager = entityManager;
        this.entityClass = entityClass;
    }
    
    @Override
    public Optional<T> findById(ID id) {
        T entity = entityManager.find(entityClass, id.getValue());
        return Optional.ofNullable(entity);
    }
    
    @Override
    public void save(T aggregate) {
        save(aggregate, aggregate.getVersion());
    }
    
    @Override
    public void save(T aggregate, long expectedVersion) {
        if (aggregate.getId() == null) {
            // New aggregate - persist
            aggregate.incrementVersion();
            entityManager.persist(aggregate);
        } else {
            // Existing aggregate - check version for optimistic concurrency
            T existing = entityManager.find(entityClass, aggregate.getId().getValue(), LockModeType.OPTIMISTIC);
            
            if (existing == null) {
                throw new IllegalArgumentException("Aggregate not found: " + aggregate.getId());
            }
            
            if (existing.getVersion() != expectedVersion) {
                throw new ConcurrencyException(
                    String.format("Version conflict: expected %d but found %d", 
                        expectedVersion, existing.getVersion())
                );
            }
            
            aggregate.incrementVersion();
            entityManager.merge(aggregate);
        }
    }
    
    @Override
    public void delete(T aggregate) {
        entityManager.remove(aggregate);
    }
}

