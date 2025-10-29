package com.youtube.monetizationservice.infrastructure.outbox;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "outbox")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OutboxJpaEntity {
	@Id
	@Column(name = "id", length = 64)
	private String id;

	@Column(name = "aggregate_type", nullable = false, length = 128)
	private String aggregateType;

	@Column(name = "aggregate_id", nullable = false, length = 64)
	private String aggregateId;

	@Column(name = "event_type", nullable = false, length = 128)
	private String eventType;

	@Lob
	@Column(name = "payload_json", nullable = false)
	private String payloadJson;

	@Column(name = "occurred_at", nullable = false)
	private Instant occurredAt;

	@Column(name = "processed", nullable = false)
	private boolean processed;
}
