package com.youtube.common.domain.shared.valueobjects;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.youtube.common.domain.core.ValueObject;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

import java.math.BigDecimal;
import java.util.Currency;
import java.util.Objects;

/**
 * Value object representing money with currency.
 * Immutable and validates business rules.
 * 
 * <p>Used across multiple services for monetary operations:
 * <ul>
 *   <li>monetization-service - Payments and billing</li>
 *   <li>billing-payout-service - Payout calculations</li>
 *   <li>ads-decision-service - Ad revenue calculations</li>
 * </ul>
 */
public final class Money implements ValueObject {
    
    @NotNull
    @PositiveOrZero
    private final BigDecimal amount;
    
    @NotNull
    private final Currency currency;

    @JsonCreator
    public Money(
            @JsonProperty("amount") BigDecimal amount,
            @JsonProperty("currency") Currency currency) {
        if (amount == null || currency == null) {
            throw new IllegalArgumentException("Amount and currency cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Amount cannot be negative");
        }
        this.amount = amount.setScale(currency.getDefaultFractionDigits(), BigDecimal.ROUND_HALF_UP);
        this.currency = currency;
    }

    public Money(BigDecimal amount, String currencyCode) {
        this(amount, Currency.getInstance(currencyCode));
    }

    public Money add(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.add(other.amount), this.currency);
    }

    public Money subtract(Money other) {
        validateSameCurrency(other);
        return new Money(this.amount.subtract(other.amount), this.currency);
    }

    public Money multiply(BigDecimal multiplier) {
        if (multiplier == null) {
            throw new IllegalArgumentException("Multiplier cannot be null");
        }
        return new Money(this.amount.multiply(multiplier), this.currency);
    }

    public Money divide(BigDecimal divisor) {
        if (divisor == null) {
            throw new IllegalArgumentException("Divisor cannot be null");
        }
        if (divisor.compareTo(BigDecimal.ZERO) == 0) {
            throw new IllegalArgumentException("Divisor cannot be zero");
        }
        return new Money(this.amount.divide(divisor, currency.getDefaultFractionDigits(), BigDecimal.ROUND_HALF_UP), this.currency);
    }

    public boolean isGreaterThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) > 0;
    }

    public boolean isLessThan(Money other) {
        validateSameCurrency(other);
        return this.amount.compareTo(other.amount) < 0;
    }

    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    public Money negate() {
        return new Money(this.amount.negate(), this.currency);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    public static Money zero(String currencyCode) {
        return new Money(BigDecimal.ZERO, currencyCode);
    }

    private void validateSameCurrency(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                String.format("Cannot operate on different currencies: %s and %s", 
                    this.currency, other.currency));
        }
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public Currency getCurrency() {
        return currency;
    }

    public String getCurrencyCode() {
        return currency.getCurrencyCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Money money = (Money) o;
        return Objects.equals(amount, money.amount) && Objects.equals(currency, money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return String.format("%s %s", currency.getSymbol(), amount);
    }
}

