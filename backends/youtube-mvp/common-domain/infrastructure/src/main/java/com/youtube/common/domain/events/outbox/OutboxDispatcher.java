package com.youtube.common.domain.events.outbox;

import com.youtube.common.domain.persistence.entity.OutboxEvent;
import com.youtube.common.domain.services.tracing.TraceProvider;
import io.micrometer.tracing.Span;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Background worker that dispatches outbox events to message brokers.
 * Polls the outbox periodically and publishes pending domain events.
 * 
 * <p>This component is only enabled when:
 * - MessagePublisher bean is available
 * - OutboxRepository bean is available
 * - Property {@code outbox.domain-event-publisher.enabled} is true (default: true)
 */
@Component
@ConditionalOnBean({MessagePublisher.class, OutboxRepository.class})
@ConditionalOnProperty(name = "outbox.domain-event-publisher.enabled", havingValue = "true", matchIfMissing = true)
public class OutboxDispatcher {
    
    private static final Logger log = LoggerFactory.getLogger(OutboxDispatcher.class);
    
    private static final int DEFAULT_BATCH_SIZE = 100;
    private static final Duration BACKOFF_DURATION = Duration.ofSeconds(5);
    
    private final OutboxRepository outboxRepository;
    private final MessagePublisher messagePublisher;
    private final TraceProvider traceProvider;
    
    private volatile boolean enabled = true;
    
    public OutboxDispatcher(
        OutboxRepository outboxRepository,
        MessagePublisher messagePublisher,
        TraceProvider traceProvider
    ) {
        this.outboxRepository = outboxRepository;
        this.messagePublisher = messagePublisher;
        this.traceProvider = traceProvider;
    }
    
    /**
     * Scheduled task that polls for pending outbox events and dispatches them.
     * Runs every 5 seconds by default.
     */
    @Scheduled(fixedDelayString = "${outbox.domain-event-publisher.interval:5000}")
    public void dispatchPendingEvents() {
        if (!enabled) {
            return;
        }
        
        Span span = traceProvider.startSpan("publish outbox batch");
        
        try {
            // Fetch pending events (uses SELECT ... FOR UPDATE SKIP LOCKED)
            var events = outboxRepository.fetchPendingBatch(DEFAULT_BATCH_SIZE);
            
            if (events.isEmpty()) {
                log.debug("No pending outbox events to dispatch");
                return;
            }
            
            log.info("Dispatching {} outbox events", events.size());
            
            // Publish each event
            for (OutboxEvent event : events) {
                try {
                    messagePublisher.publish(event);
                    String brokerMessageId = messagePublisher.getBrokerMessageId(event);
                    outboxRepository.markDispatched(event.getId(), brokerMessageId);
                    log.debug("Dispatched event {} to message broker", event.getId());
                } catch (MessagePublisher.MessagePublishException e) {
                    log.error("Failed to dispatch event {}", event.getId(), e);
                    outboxRepository.markFailed(event.getId(), truncate(e.getMessage(), 3900));
                } catch (Exception e) {
                    log.error("Unexpected error dispatching event {}", event.getId(), e);
                    outboxRepository.markFailed(event.getId(), truncate(e.getMessage(), 3900));
                }
            }
            
            log.info("Successfully dispatched {} outbox events", events.size());
            
        } catch (Exception e) {
            log.error("Error during outbox dispatch", e);
            traceProvider.endSpan(span, e);
        } finally {
            traceProvider.endSpan(span);
        }
    }
    private static String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() <= max ? s : s.substring(0, max);
    }
    
    /**
     * Enables the dispatcher.
     */
    public void enable() {
        this.enabled = true;
    }
    
    /**
     * Disables the dispatcher.
     */
    public void disable() {
        this.enabled = false;
    }
}

