package com.youtube.monetizationservice.infrastructure.outbox;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboxJpaRepository extends JpaRepository<OutboxJpaEntity, String> {
	List<OutboxJpaEntity> findTop50ByProcessedFalseOrderByOccurredAtAsc();
}
