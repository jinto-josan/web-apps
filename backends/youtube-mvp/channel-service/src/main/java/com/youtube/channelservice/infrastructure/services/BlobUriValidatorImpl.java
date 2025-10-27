package com.youtube.channelservice.infrastructure.services;

import com.youtube.channelservice.domain.services.BlobUriValidator;
import com.youtube.channelservice.shared.exceptions.ValidationException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.util.Set;

/**
 * Infrastructure implementation of BlobUriValidator.
 * Validates blob URIs against allowed origins.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BlobUriValidatorImpl implements BlobUriValidator {
    
    private final Set<String> allowedOrigins;
    
    public BlobUriValidatorImpl(@org.springframework.beans.factory.annotation.Value("${blob.allowed-origins}") Set<String> allowedOrigins) {
        this.allowedOrigins = Set.copyOf(allowedOrigins);
    }
    
    @Override
    public void validate(String uri) {
        if (uri == null || uri.isBlank()) {
            return; // Allow null/empty URIs
        }
        
        try {
            URI parsedUri = URI.create(uri);
            String origin = parsedUri.getScheme() + "://" + parsedUri.getHost();
            
            if (!allowedOrigins.contains(origin)) {
                throw new ValidationException("URI origin not allowed: " + origin);
            }
            
            log.debug("URI validation passed for: {}", uri);
            
        } catch (Exception e) {
            log.error("URI validation failed for: {}", uri, e);
            throw new ValidationException("Invalid URI format: " + uri, e);
        }
    }
    
    @Override
    public Set<String> getAllowedOrigins() {
        return Set.copyOf(allowedOrigins);
    }
}
