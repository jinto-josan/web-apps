package com.youtube.monetizationservice.domain.valueobjects;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Value object representing ledger entry type for double-entry bookkeeping.
 */
@Getter
@AllArgsConstructor
public enum LedgerEntryType {
    DEBIT("Debit entry - money out"),
    CREDIT("Credit entry - money in");

    private final String description;
}

