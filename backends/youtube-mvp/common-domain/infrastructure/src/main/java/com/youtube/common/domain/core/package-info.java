/**
 * Core domain-driven design building blocks.
 * 
 * <p>This package contains the fundamental DDD abstractions used across all services:
 * <ul>
 *   <li>{@link com.youtube.common.domain.core.Entity} - Base class for domain entities</li>
 *   <li>{@link com.youtube.common.domain.core.AggregateRoot} - Base class for aggregate roots</li>
 *   <li>{@link com.youtube.common.domain.core.DomainEvent} - Base class for domain events</li>
 *   <li>{@link com.youtube.common.domain.core.Identifier} - Interface for domain identifiers</li>
 *   <li>{@link com.youtube.common.domain.core.ValueObject} - Marker interface for value objects</li>
 *   <li>{@link com.youtube.common.domain.core.Clock} - Time abstraction</li>
 *   <li>{@link com.youtube.common.domain.core.IdGenerator} - Identifier generation</li>
 *   <li>{@link com.youtube.common.domain.core.UnitOfWork} - Transaction management</li>
 * </ul>
 */
package com.youtube.common.domain.core;

