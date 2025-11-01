package com.youtube.common.domain.core;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for aggregate roots.
 * Aggregate roots are entities that serve as entry points to aggregates.
 * They manage domain events and handle optimistic concurrency control.
 * 
 * @param <ID> the type of the aggregate's identifier
 */
public abstract class AggregateRoot<ID extends Identifier<?>> extends Entity<ID> {
    
    private long version = 0;
    private final List<DomainEvent> pendingEvents = new ArrayList<>();
    
    protected AggregateRoot() {
        // For JPA
    }
    
    protected AggregateRoot(ID id) {
        super(id);
        this.version = 0;
    }
    
    protected AggregateRoot(ID id, long version) {
        super(id);
        this.version = version;
    }
    
    public long getVersion() {
        return version;
    }
    
    protected void setVersion(long version) {
        this.version = version;
    }
    
    /**
     * Records a domain event for later publishing.
     * 
     * @param event the domain event to record
     */
    protected void record(DomainEvent event) {
        if (event == null) {
            throw new IllegalArgumentException("Domain event cannot be null");
        }
        pendingEvents.add(event);
        apply(event);
    }
    
    /**
     * Applies the event to update the aggregate's state.
     * Subclasses should override this to handle event sourcing if needed.
     * 
     * @param event the domain event to apply
     */
    protected void apply(DomainEvent event) {
        // Default implementation does nothing
        // Subclasses can override for event sourcing
    }
    
    /**
     * Returns the list of pending domain events.
     * 
     * @return an unmodifiable list of pending events
     */
    public List<DomainEvent> pendingDomainEvents() {
        return Collections.unmodifiableList(pendingEvents);
    }
    
    /**
     * Marks all pending events as committed.
     * Called after events have been successfully published.
     */
    public void markEventsCommitted() {
        pendingEvents.clear();
    }
    
    /**
     * Increments the version for optimistic concurrency control.
     */
    public void incrementVersion() {
        this.version++;
    }
}

