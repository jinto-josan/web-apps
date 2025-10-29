package com.youtube.monetizationservice.infrastructure.persistence.jpa.repository;

import com.youtube.monetizationservice.infrastructure.persistence.jpa.entity.LedgerJpaEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.List;

public interface LedgerJpaRepository extends JpaRepository<LedgerJpaEntity, String> {
	List<LedgerJpaEntity> findByTransactionRef(String transactionRef);
	List<LedgerJpaEntity> findByAccountCode(String accountCode);
	List<LedgerJpaEntity> findByTransactionDateBetween(Instant startDate, Instant endDate);
}
