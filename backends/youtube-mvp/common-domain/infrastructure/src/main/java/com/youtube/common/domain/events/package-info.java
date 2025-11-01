/**
 * Event-driven infrastructure for reliable event publishing and consumption.
 * 
 * <p>This package provides:
 * <ul>
 *   <li>{@link com.youtube.common.domain.events.EventPublisher} - Publishes events using transactional outbox</li>
 *   <li>{@link com.youtube.common.domain.events.EventProcessor} - Consumes events with inbox idempotency</li>
 *   <li>{@link com.youtube.common.domain.events.EventRouter} - Routes events to handlers</li>
 * </ul>
 * 
 * <p>Sub-packages:
 * <ul>
 *   <li>{@link com.youtube.common.domain.events.outbox} - Transactional outbox pattern</li>
 *   <li>{@link com.youtube.common.domain.events.inbox} - Inbox pattern for idempotent processing</li>
 * </ul>
 */
package com.youtube.common.domain.events;

