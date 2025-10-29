package com.youtube.monetizationservice.domain.service;

import com.youtube.monetizationservice.domain.valueobjects.Money;

/**
 * Domain service for payment-related business logic.
 */
public interface PaymentDomainService {
    
    /**
     * Validates payment amount matches expected amount.
     * 
     * @param paymentAmount the payment amount
     * @param expectedAmount the expected amount
     * @return true if amounts match
     */
    boolean validatePaymentAmount(Money paymentAmount, Money expectedAmount);
    
    /**
     * Verifies webhook signature from payment provider.
     * 
     * @param payload the webhook payload
     * @param signature the signature to verify
     * @param secret the secret key
     * @return true if signature is valid
     */
    boolean verifyWebhookSignature(String payload, String signature, String secret);
}

