package com.youtube.common.domain;

import java.util.Objects;

/**
 * Abstract base class for domain entities.
 * Entities are objects that have a distinct identity and lifecycle.
 * 
 * @param <ID> the type of the entity's identifier
 */
public abstract class Entity<ID extends Identifier> {
    protected final ID id;

    protected Entity(ID id) {
        this.id = Objects.requireNonNull(id, "Entity ID cannot be null");
    }

    /**
     * Gets the unique identifier of this entity.
     * 
     * @return the entity's identifier
     */
    public ID getId() {
        return id;
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

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{id=" + id + "}";
    }
}
