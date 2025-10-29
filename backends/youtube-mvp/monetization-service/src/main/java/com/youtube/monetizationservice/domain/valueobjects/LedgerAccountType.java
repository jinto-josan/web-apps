package com.youtube.monetizationservice.domain.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Value object representing ledger account type.
 */
@Getter
@AllArgsConstructor
public enum LedgerAccountType {
    ASSET("Asset account"),
    LIABILITY("Liability account"),
    REVENUE("Revenue account"),
    EXPENSE("Expense account"),
    EQUITY("Equity account");

    private final String description;
}

