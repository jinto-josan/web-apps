package com.youtube.monetizationservice.domain.service.impl;

import com.youtube.monetizationservice.domain.service.PaymentDomainService;
import com.youtube.monetizationservice.domain.valueobjects.Money;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * Implementation of payment domain service.
 */
@Slf4j
@Service
public class PaymentDomainServiceImpl implements PaymentDomainService {
    
    @Override
    public boolean validatePaymentAmount(Money paymentAmount, Money expectedAmount) {
        if (!paymentAmount.getCurrency().equals(expectedAmount.getCurrency())) {
            log.warn("Currency mismatch: {} vs {}", paymentAmount.getCurrency(), expectedAmount.getCurrency());
            return false;
        }
        
        boolean valid = paymentAmount.getAmount().compareTo(expectedAmount.getAmount()) == 0;
        if (!valid) {
            log.warn("Amount mismatch: {} vs {}", paymentAmount.getAmount(), expectedAmount.getAmount());
        }
        
        return valid;
    }
    
    @Override
    public boolean verifyWebhookSignature(String payload, String signature, String secret) {
        try {
            Mac hmac = Mac.getInstance("HmacSHA256");
            SecretKeySpec secretKeySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
            hmac.init(secretKeySpec);
            
            byte[] hash = hmac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            String expectedSignature = bytesToHex(hash);
            
            return expectedSignature.equals(signature);
        } catch (NoSuchAlgorithmException | InvalidKeyException e) {
            log.error("Error verifying webhook signature", e);
            return false;
        }
    }
    
    private String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString();
    }
}

