package com.youtube.common.domain.repository;

import com.youtube.common.domain.core.AggregateRoot;
import com.youtube.common.domain.core.Identifier;
import com.youtube.common.domain.core.ConcurrencyException;

import java.util.Optional;

/**
 * Base repository interface for aggregate roots.
 * Supports optimistic concurrency control with version checking.
 * 
 * @param <T> the aggregate root type
 * @param <ID> the identifier type
 */
public interface Repository<T extends AggregateRoot<ID>, ID extends Identifier<?>> {
    
    /**
     * Finds an aggregate by its ID.
     * 
     * @param id the aggregate ID
     * @return the aggregate, or empty if not found
     */
    Optional<T> findById(ID id);
    
    /**
     * Saves an aggregate with optimistic concurrency control.
     * 
     * @param aggregate the aggregate to save
     * @throws ConcurrencyException if the version doesn't match
     */
    void save(T aggregate);
    
    /**
     * Saves an aggregate with explicit version checking.
     * 
     * @param aggregate the aggregate to save
     * @param expectedVersion the expected version (must match current version)
     * @throws ConcurrencyException if the version doesn't match
     */
    void save(T aggregate, long expectedVersion);
    
    /**
     * Deletes an aggregate.
     * 
     * @param aggregate the aggregate to delete
     */
    void delete(T aggregate);
}
