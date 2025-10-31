package com.youtube.contentidservice.infrastructure.messaging;

import com.azure.messaging.servicebus.ServiceBusMessage;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.youtube.contentidservice.infrastructure.persistence.JpaOutboxRepository;
import com.youtube.contentidservice.infrastructure.persistence.entity.OutboxEventJpaEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class OutboxDispatcher {
    private final JpaOutboxRepository outboxRepository;
    private final ServiceBusSenderClient serviceBusSenderClient;
    private final ObjectMapper objectMapper;

    @Scheduled(fixedRate = 5000) // Every 5 seconds
    @Transactional
    public void dispatchPendingEvents() {
        if (serviceBusSenderClient == null) {
            log.debug("Service Bus not configured, skipping dispatch");
            return;
        }

        List<OutboxEventJpaEntity> pendingEvents = outboxRepository.findPendingEvents();

        if (pendingEvents.isEmpty()) {
            return;
        }

        log.debug("Dispatching {} pending events", pendingEvents.size());

        for (OutboxEventJpaEntity event : pendingEvents) {
            try {
                ServiceBusMessage message = new ServiceBusMessage(event.getPayload())
                        .setMessageId(event.getId().toString())
                        .setSubject(event.getEventType());

                message.getApplicationProperties().put("aggregateType", event.getAggregateType());
                message.getApplicationProperties().put("aggregateId", event.getAggregateId());
                message.getApplicationProperties().put("eventType", event.getEventType());

                serviceBusSenderClient.sendMessage(message);

                // Mark as dispatched
                event.setStatus("DISPATCHED");
                event.setDispatchedAt(Instant.now());
                event.setBrokerMessageId(message.getMessageId());
                outboxRepository.save(event);

                log.debug("Dispatched outbox event: {}", event.getId());
            } catch (Exception e) {
                log.error("Failed to dispatch outbox event: {}", event.getId(), e);
                event.setRetryCount(event.getRetryCount() + 1);
                if (event.getRetryCount() >= 3) {
                    event.setStatus("FAILED");
                }
                outboxRepository.save(event);
            }
        }
    }
}

