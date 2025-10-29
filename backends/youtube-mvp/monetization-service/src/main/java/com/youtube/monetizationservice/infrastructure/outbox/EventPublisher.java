package com.youtube.monetizationservice.infrastructure.outbox;

public interface EventPublisher {
	void publish(Object domainEvent);
}
