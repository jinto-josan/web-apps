package com.youtube.common.domain.core;

import java.util.Objects;

/**
 * Base class for domain entities.
 * Entities are objects that have identity and are mutable.
 * 
 * @param <ID> the type of the entity's identifier
 */
public abstract class Entity<ID extends Identifier<?>> {
    
    protected ID id;
    
    protected Entity() {
        // For JPA
    }
    
    protected Entity(ID id) {
        this.id = Objects.requireNonNull(id, "Entity ID cannot be null");
    }
    
    public ID getId() {
        return id;
    }
    
    protected void setId(ID id) {
        this.id = Objects.requireNonNull(id, "Entity ID cannot be null");
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Entity<?> entity = (Entity<?>) o;
        return Objects.equals(id, entity.id);
    }
    
    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}

