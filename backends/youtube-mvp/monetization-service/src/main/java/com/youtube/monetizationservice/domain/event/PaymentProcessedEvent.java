package com.youtube.monetizationservice.domain.event;

import com.youtube.common.domain.DomainEvent;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * Domain event fired when a payment is processed.
 */
@Getter
public class PaymentProcessedEvent extends DomainEvent {
    private final String paymentId;
    private final String invoiceId;
    private final String membershipId;
    private final BigDecimal amount;
    private final String currency;
    private final String status;
    
    public PaymentProcessedEvent(String paymentId, String invoiceId, String membershipId, BigDecimal amount, String currency, String status) {
        super();
        this.paymentId = paymentId;
        this.invoiceId = invoiceId;
        this.membershipId = membershipId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }
    
    public PaymentProcessedEvent(String eventId, Instant occurredAt, String paymentId, String invoiceId, String membershipId, BigDecimal amount, String currency, String status) {
        super(eventId, occurredAt);
        this.paymentId = paymentId;
        this.invoiceId = invoiceId;
        this.membershipId = membershipId;
        this.amount = amount;
        this.currency = currency;
        this.status = status;
    }
}

