package com.youtube.monetizationservice.domain.repository;

import com.youtube.monetizationservice.domain.models.Payment;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Payment aggregate.
 */
public interface PaymentRepository {
    
    Payment save(Payment payment);
    
    Optional<Payment> findById(String paymentId);
    
    Optional<Payment> findByExternalPaymentId(String externalPaymentId);
    
    List<Payment> findByInvoiceId(String invoiceId);
    
    List<Payment> findByMembershipId(String membershipId);
    
    boolean existsById(String paymentId);
}

