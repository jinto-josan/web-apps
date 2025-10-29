package com.youtube.monetizationservice.infrastructure.outbox;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;
import com.azure.messaging.servicebus.ServiceBusSenderClient;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventPublisherImpl implements EventPublisher {

	private final OutboxJpaRepository outboxRepository;
	private final ObjectMapper objectMapper;
	private final ServiceBusClientBuilder serviceBusClientBuilder;

	@Value("${azure.servicebus.topics.billing-events:billing.events}")
	private String topicName;

	@Value("${azure.servicebus.connection-string:}")
	private String serviceBusConnectionString;

	@Override
	@Transactional
	public void publish(Object domainEvent) {
		try {
			String payload = objectMapper.writeValueAsString(domainEvent);
			OutboxJpaEntity outbox = OutboxJpaEntity.builder()
				.id(UUID.randomUUID().toString())
				.aggregateType(domainEvent.getClass().getSimpleName())
				.aggregateId("-")
				.eventType(domainEvent.getClass().getName())
				.payloadJson(payload)
				.occurredAt(Instant.now())
				.processed(false)
				.build();
			outboxRepository.save(outbox);

			if (serviceBusConnectionString != null && !serviceBusConnectionString.isEmpty()) {
				ServiceBusSenderClient sender = serviceBusClientBuilder
					.connectionString(serviceBusConnectionString)
					.sender()
					.topicName(topicName)
					.buildClient();
				sender.sendMessage(new com.azure.messaging.servicebus.ServiceBusMessage(payload));
				sender.close();
				outbox.setProcessed(true);
				outboxRepository.save(outbox);
			}
		} catch (JsonProcessingException e) {
			log.error("Failed to serialize event", e);
			throw new RuntimeException(e);
		}
	}
}
