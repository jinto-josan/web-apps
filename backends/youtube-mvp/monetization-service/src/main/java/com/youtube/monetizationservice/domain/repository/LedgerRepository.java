package com.youtube.monetizationservice.domain.repository;

import com.youtube.monetizationservice.domain.models.Ledger;

import java.util.List;

/**
 * Repository interface for Ledger entries.
 */
public interface LedgerRepository {
    
    Ledger save(Ledger ledger);
    
    List<Ledger> findByTransactionRef(String transactionRef);
    
    List<Ledger> findByAccountCode(String accountCode);
    
    List<Ledger> findByTransactionDateBetween(java.time.Instant startDate, java.time.Instant endDate);
}

