/**
 * Inbox pattern implementation for idempotent event processing.
 * 
 * <p>Ensures events are processed exactly once by tracking which messages
 * have already been processed using a database-backed inbox.</p>
 * 
 * <p>Components:
 * <ul>
 *   <li>{@link com.youtube.common.domain.events.inbox.InboxRepository} - Repository interface</li>
 *   <li>{@link com.youtube.common.domain.events.inbox.JpaInboxRepository} - JPA implementation</li>
 * </ul>
 */
package com.youtube.common.domain.events.inbox;

