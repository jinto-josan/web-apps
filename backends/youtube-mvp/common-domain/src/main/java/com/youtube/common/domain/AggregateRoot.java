package com.youtube.common.domain;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Abstract base class for aggregate roots.
 * Aggregate roots are entities that serve as the entry point to an aggregate
 * and are responsible for maintaining consistency within the aggregate.
 * 
 * @param <ID> the type of the aggregate's identifier
 */
public abstract class AggregateRoot<ID extends Identifier> extends Entity<ID> {
    private long version;
    private final List<DomainEvent> pendingEvents;

    protected AggregateRoot(ID id) {
        super(id);
        this.version = 0;
        this.pendingEvents = new ArrayList<>();
    }

    /**
     * Gets the current version of this aggregate.
     * 
     * @return the aggregate version
     */
    public long getVersion() {
        return version;
    }

    /**
     * Sets the version of this aggregate.
     * This is typically used by the repository when loading the aggregate.
     * 
     * @param version the new version
     */
    public void setVersion(long version) {
        this.version = version;
    }

    /**
     * Records a domain event that will be published when the aggregate is saved.
     * 
     * @param event the domain event to record
     */
    protected void record(DomainEvent event) {
        Objects.requireNonNull(event, "Domain event cannot be null");
        this.pendingEvents.add(event);
    }

    /**
     * Marks all pending events as committed.
     * This should be called after the events have been successfully published.
     */
    public void markEventsCommitted() {
        this.pendingEvents.clear();
    }

    /**
     * Gets all pending domain events.
     * 
     * @return an unmodifiable list of pending events
     */
    public List<DomainEvent> pendingDomainEvents() {
        return Collections.unmodifiableList(pendingEvents);
    }

    /**
     * Applies a domain event to this aggregate.
     * This method should be implemented by concrete aggregate roots to handle
     * domain events for state reconstruction.
     * 
     * @param event the domain event to apply
     */
    protected abstract void apply(DomainEvent event);

    /**
     * Increments the version of this aggregate.
     * This should be called when the aggregate is modified.
     */
    protected void incrementVersion() {
        this.version++;
    }
}
