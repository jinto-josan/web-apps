/**
 * Transactional outbox pattern implementation.
 * 
 * <p>Provides reliable event publishing by storing events in the database
 * within the same transaction as business logic, then dispatching them
 * asynchronously to the message broker.</p>
 * 
 * <p>Components:
 * <ul>
 *   <li>{@link com.youtube.common.domain.events.outbox.OutboxRepository} - Repository interface</li>
 *   <li>{@link com.youtube.common.domain.events.outbox.JpaOutboxRepository} - JPA implementation</li>
 *   <li>{@link com.youtube.common.domain.events.outbox.OutboxDispatcher} - Background dispatcher worker</li>
 * </ul>
 */
package com.youtube.common.domain.events.outbox;

