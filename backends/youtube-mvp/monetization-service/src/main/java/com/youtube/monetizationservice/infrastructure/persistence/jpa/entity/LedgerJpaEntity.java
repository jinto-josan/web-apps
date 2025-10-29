package com.youtube.monetizationservice.infrastructure.persistence.jpa.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "ledger", indexes = {
	@Index(name = "idx_ledger_tx_ref", columnList = "transaction_ref"),
	@Index(name = "idx_ledger_account_code", columnList = "account_code")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LedgerJpaEntity {
	@Id
	@Column(name = "id", nullable = false, length = 64)
	private String id;

	@Column(name = "transaction_ref", nullable = false, length = 64)
	private String transactionRef;

	@Column(name = "account_type", nullable = false, length = 32)
	private String accountType;

	@Column(name = "account_code", nullable = false, length = 64)
	private String accountCode;

	@Column(name = "entry_type", nullable = false, length = 16)
	private String entryType;

	@Column(name = "amount", nullable = false, precision = 19, scale = 4)
	private BigDecimal amount;

	@Column(name = "currency", nullable = false, length = 3)
	private String currency;

	@Column(name = "transaction_date", nullable = false)
	private Instant transactionDate;

	@Column(name = "description")
	private String description;

	@Column(name = "related_ref", length = 64)
	private String relatedRef;

	@Version
	@Column(name = "version", nullable = false)
	private Integer version;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt;

	@Column(name = "updated_at", nullable = false)
	private Instant updatedAt;
}
