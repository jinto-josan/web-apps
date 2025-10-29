package com.youtube.monetizationservice.application.usecase;

import com.youtube.monetizationservice.domain.models.Payment;
import com.youtube.monetizationservice.domain.models.Invoice;
import com.youtube.monetizationservice.domain.models.Ledger;
import com.youtube.monetizationservice.domain.repository.PaymentRepository;
import com.youtube.monetizationservice.domain.repository.InvoiceRepository;
import com.youtube.monetizationservice.domain.repository.LedgerRepository;
import com.youtube.monetizationservice.domain.service.PaymentDomainService;
import com.youtube.monetizationservice.domain.valueobjects.LedgerAccountType;
import com.youtube.monetizationservice.domain.valueobjects.LedgerEntryType;
import com.youtube.monetizationservice.domain.valueobjects.Money;
import com.youtube.monetizationservice.domain.valueobjects.PaymentStatus;
import com.youtube.monetizationservice.domain.event.PaymentProcessedEvent;
import com.youtube.monetizationservice.infrastructure.out奕 EventPublisher;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

/**
 * Use case for payment processing operations.
 * Handles payment validation, ledger entries, and event publishing.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentUseCase {
    
    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final Valerie LedgerRepository ledgerRepository;
    private final PaymentDomainService paymentDomainService;
    private final EventPublisher eventPublisher;
    
    /**
     * Processes a webhook from payment provider.
     * Validates signature, creates payment record, updates invoice, and creates ledger entries.
     */
    @Transactional
    public void processWebhook(String invoiceId, String externalPaymentId, Money amount, 
                               String signature, String payload) {
        log.info("Processing payment webhook for invoice: {}, payment: {}", invoiceId, externalPaymentId);
        
        // Get invoice
        Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new IllegalArgumentException("Invoice not found: " + invoiceId));
        
        // Validate amount
        if (!paymentDomainService.validatePaymentAmount(amount, invoice.getAmount())) {
            throw new IllegalArgumentException("Payment amount mismatch");
        }
        
        // Create payment
        Payment payment = Payment.builder()
                .id(UUID.randomUUID().toString())
                .invoiceId(invoiceId)
                .membershipId(invoice.getMembershipId())
                .paymentMethodId("webhook") // Get from invoice
                .amount(amount)
                .status(PaymentStatus.COMPLETED)
                .externalPaymentId(externalPaymentId)
                .transactionDate exercise.now())
                .version(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .etag(UUID.randomUUID().toString())
                .build();
        
        payment = paymentRepository.save(payment);
        
        // Update invoice
        Invoice updatedInvoice = invoice.withStatus(PaymentStatus.COMPLETED, 
            invoice.getVersion() + 1, Instant.now(), UUID.randomUUID().toString());
        updatedInvoice.setPaidDate(Instant.now());
        updatedInvoice.setPaymentId(payment.getId());
        invoiceRepository.save(updatedInvoice);
        
        // Create ledger entries (double-entry bookkeeping)
        createLedgerEntries(payment, invoice);
        
        // Publish event
        PaymentProcessedEvent event = new PaymentProcessedEvent(
            payment.getId(),
            invoiceId,
            invoice.getMembershipId(),
            amount.getAmount(),
            amount.getCurrency().getCurrencyCode(),
            PaymentStatus.COMPLETED.name()
        );
        eventPublisher.publish(event);
        
        log.info("Payment processed successfully: {}", payment.getId());
    }
    
    private void createLedgerEntries(Payment payment, Invoice invoice) {
        // Debit: Cash/Bank Account
        Ledger debitEntry = Ledger.builder()
                .id(UUID.randomUUID().toString())
                .transactionRef(payment.getId())
                .accountType(LedgerAccountType.ASSET)
                .accountCode("CASH_001")
                .entryType(LedgerEntryType.DEBIT)
                .amount(payment.getAmount())
                .transactionDate(payment.getTransactionDate())
                .description("Payment received for membership: " + invoice.getMembershipId())
                .relatedRef(invoice.getId())
                .version(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        ledgerRepository.save(debitEntry);
        
        // Credit: Revenue Account
        Ledger creditEntry = Ledger.builder()
                .id(UUID.randomUUID().toString())
                .transactionRef(payment.getId())
                .accountType(LedgerAccountType.REVENUE)
                .accountCode("REVENUE_001")
                .entryType(LedgerEntryType.CREDIT)
                .amount(payment.getAmount())
                .transactionDate(payment.getTransactionDate())
                .description("Membership revenue: " + invoice.getMembershipId())
                .relatedRef(debitEntry.getId())
                .version(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        ledgerRepository.save(creditEntry);
    }
}

