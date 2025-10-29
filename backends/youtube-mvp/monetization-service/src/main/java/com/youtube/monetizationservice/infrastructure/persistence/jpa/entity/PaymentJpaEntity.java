package com.youtube.monetizationservice.infrastructure.persistence.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "payments", indexes = {
	@Index(name = "idx_payments_invoice", columnList = "invoice_id"),
	@Index(name = "idx_payments_membership", columnList = "membership_id")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaymentJpaEntity {
	@Id
	@Column(name = "id", nullable = false, length = 64)
	private String id;

	@Column(name = "invoice_id", nullable = false, length = 64)
	private String invoiceId;

	@Column(name = "membership_id", nullable = false, length = 64)
	private String membershipId;

	@Column(name = "payment_method_id", nullable = false, length = 128)
	private String paymentMethodId;

	@Column(name = "amount", nullable = false, precision = 19, scale = 4)
	private BigDecimal amount;

	@Column(name = "currency", nullable = false, length = 3)
	private String currency;

	@Column(name = "status", nullable = false, length = 32)
	private String status;

	@Column(name = "external_payment_id", nullable = false, unique = true, length = 128)
	private String externalPaymentId;

	@Column(name = "transaction_date", nullable = false)
	private Instant transactionDate;

	@Column(name = "failure_reason")
	private String failureReason;

	@Column(name = "refunded_date")
	private Instant refundedDate;

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
