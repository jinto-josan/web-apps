package com.youtube.common.domain.core;

import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/**
 * Service for managing units of work (transactions).
 * Provides explicit transaction boundaries for command handling.
 */
@Component
public class UnitOfWork {
    
    /**
     * Begins a new transaction.
     * If already in a transaction, this is a no-op.
     */
    public void begin() {
        // Spring @Transactional handles this automatically
        // This method exists for explicit transaction boundaries
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("No active transaction. Use @Transactional annotation on your method.");
        }
    }
    
    /**
     * Commits the current transaction.
     * This is handled automatically by Spring @Transactional.
     */
    @Transactional
    public void commit() {
        // Spring handles commit automatically when method exits successfully
        if (!TransactionSynchronizationManager.isActualTransactionActive()) {
            throw new IllegalStateException("No active transaction to commit");
        }
    }
    
    /**
     * Rolls back the current transaction.
     * 
     * @param cause the exception that caused the rollback
     */
    public void rollback(Throwable cause) {
        // In Spring, rollback is typically triggered by throwing an exception
        // For explicit rollback, throw a RuntimeException
        if (cause instanceof RuntimeException) {
            throw (RuntimeException) cause;
        } else if (cause instanceof Error) {
            throw (Error) cause;
        } else {
            throw new RuntimeException("Transaction rollback", cause);
        }
    }
    
    /**
     * Checks if there is an active transaction.
     * 
     * @return true if there is an active transaction
     */
    public boolean isActive() {
        return TransactionSynchronizationManager.isActualTransactionActive();
    }
}

