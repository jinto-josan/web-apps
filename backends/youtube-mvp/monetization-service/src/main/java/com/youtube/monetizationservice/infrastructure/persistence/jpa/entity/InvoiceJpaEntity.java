package com.youtube.monetizationservice.infrastructure.persistence.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "invoices")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceJpaEntity {
	@Id
	@Column(name = "id", nullable = false, length = 64)
	private String id;

	@Column(name = "membership_id", nullable = false, length = 64)
	private String membershipId;

	@Column(name = "channel_id", nullable = false, length = 128)
	private String channelId;

	@Column(name = "subscriber_id", nullable = false, length = 128)
	private String subscriberId;

	@Column(name = "amount", nullable = false, precision = 19, scale = 4)
	private BigDecimal amount;

	@Column(name = "currency", nullable = false, length = 3)
	private String currency;

	@Column(name = "status", nullable = false, length = 32)
	private String status;

	@Column(name = "due_date", nullable = false)
	private Instant dueDate;

	@Column(name = "paid_date")
	private Instant paidDate;

	@Column(name = "payment_id", length = 64)
	private String paymentId;

	@Version
	@Column(name = "version", nullable = false)
	private Integer version;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;

	@Column(name = "etag", length = 128)
	private String etag;
}
