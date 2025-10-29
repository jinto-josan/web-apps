package com.youtube.monetizationservice.domain.repository;

import com.youtube.monetizationservice.domain.models.Invoice;

import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Invoice aggregate.
 */
public interface InvoiceRepository {
    
    Invoice save(Invoice invoice);
    
    Optional<Invoice> findById(String invoiceId);
    
    List<Invoice> findByMembershipId(String membershipId);
    
    List<Invoice> findByChannelId(String channelId);
    
    List<Invoice> findBySubscriberId(String subscriberId);
    
    boolean existsById(String invoiceId);
}

