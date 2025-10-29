package com.youtube.monetizationservice.domain.models;

import com.youtube.monetizationservice.domain.valueobjects.LedgerAccountType;
import com.youtube.monetizationservice.domain.valueobjects.LedgerEntryType;
import com.youtube.monetizationservice.domain.valueobjects.Money;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.time.Instant;

/**
 * Domain entity representing a ledger entry for double-entry bookkeeping.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ledger {
    
    @NotBlank(message = "Ledger ID cannot be blank")
    private String id; // ULID
    
    @NotBlank(message = "Transaction reference cannot be blank")
    private String transactionRef; // Reference to source transaction (invoice, payment, etc.)
    
    @NotNull(message = "Account type cannot be null")
    private LedgerAccountType accountType;
    
    @NotBlank(message = "Account code cannot be blank")
    private String accountCode;
    
    @NotNull(message = "Entry type cannot be null")
    private LedgerEntryType entryType;
    
    @NotNull(message = "Amount cannot be null")
    private Money amount;
    
    @NotNull(message = "Transaction date cannot be null")
    private Instant transactionDate;
    
    private String description;
    private String relatedRef; // Reference to related transaction (for double-entry pairing)
    
    @Builder.Default
    private int version = 1;
    
    @NotNull(message = "Created at timestamp cannot be null")
    private Instant createdAt;
    
    @NotNull(message = "Updated at timestamp cannot be null")
    private Instant updatedAt;
    
    /**
     * Creates a new ledger entry with updated timestamp.
     */
    public Ledger withUpdatedAt(Instant newUpdatedAt, int newVersion) {
        return this.toBuilder()
                .updatedAt(newUpdatedAt)
                .version(newVersion)
                .build();
    }
}

