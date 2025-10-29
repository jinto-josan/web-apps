package com.youtube.monetizationservice.infrastructure.persistence.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "memberships", uniqueConstraints = {
	@UniqueConstraint(name = "uk_membership_channel_subscriber", columnNames = {"channel_id", "subscriber_id"})
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipJpaEntity {
	@Id
	@Column(name = "id", nullable = false, length = 64)
	private String id;

	@Column(name = "channel_id", nullable = false, length = 128)
	private String channelId;

	@Column(name = "subscriber_id", nullable = false, length = 128)
	private String subscriberId;

	@Column(name = "tier", nullable = false, length = 32)
	private String tier;

	@Column(name = "monthly_fee_amount", nullable = false, precision = 19, scale = 4)
	private BigDecimal monthlyFeeAmount;

	@Column(name = "monthly_fee_currency", nullable = false, length = 3)
	private String monthlyFeeCurrency;

	@Column(name = "status", nullable = false, length = 32)
	private String status;

	@Column(name = "start_date", nullable = false)
	private Instant startDate;

	@Column(name = "end_date")
	private Instant endDate;

	@Column(name = "next_billing_date")
	private Instant nextBillingDate;

	@Column(name = "payment_method_id", length = 128)
	private String paymentMethodId;

	@Column(name = "external_subscription_id", length = 128)
	private String externalSubscriptionId;

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
