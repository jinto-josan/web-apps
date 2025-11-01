package com.youtube.common.domain.services.tenant;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Service for resolving tenant context from JWT claims and request headers.
 * Supports multi-tenant request context resolution.
 */
@Component
public class TenantResolver {
    
    private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(TenantResolver.class);
    
    private final TenantRepository tenantRepository;
    private final TokenValidator tokenValidator;
    
    public TenantResolver(TenantRepository tenantRepository, TokenValidator tokenValidator) {
        this.tenantRepository = tenantRepository;
        this.tokenValidator = tokenValidator;
    }
    
    /**
     * Convenience constructor that creates a default token validator.
     * Services can provide their own implementation.
     */
    public TenantResolver(TenantRepository tenantRepository) {
        this.tenantRepository = tenantRepository;
        this.tokenValidator = null; // Services must provide their own
    }
    
    /**
     * Resolves the tenant ID from JWT claims and request context.
     * 
     * @param authorizationHeader the Authorization header value
     * @param host the request host
     * @param headers additional headers for tenant resolution
     * @return the tenant ID
     * @throws TenantResolutionException if tenant cannot be resolved
     */
    public String resolve(String authorizationHeader, String host, java.util.Map<String, String> headers) {
        if (tokenValidator == null) {
            throw new IllegalStateException("TokenValidator must be provided");
        }
        
        // Validate JWT token
        TokenClaims claims = tokenValidator.validate(authorizationHeader);
        
        // Extract tenant ID from claims
        String tenantIdFromClaims = claims.tenantId();
        String tenantDomainFromClaims = claims.tenantDomain();
        
        // Try to resolve by tenant ID
        if (tenantIdFromClaims != null) {
            Optional<Tenant> tenant = tenantRepository.findById(tenantIdFromClaims);
            if (tenant.isPresent()) {
                log.debug("Resolved tenant {} from claims", tenantIdFromClaims);
                return tenant.get().getId();
            }
        }
        
        // Try to resolve by domain/host
        if (host != null) {
            Optional<Tenant> tenant = tenantRepository.findByDomain(host);
            if (tenant.isPresent()) {
                log.debug("Resolved tenant {} from host {}", tenant.get().getId(), host);
                return tenant.get().getId();
            }
        }
        
        // Try to resolve by tenant domain from claims
        if (tenantDomainFromClaims != null) {
            Optional<Tenant> tenant = tenantRepository.findByDomain(tenantDomainFromClaims);
            if (tenant.isPresent()) {
                log.debug("Resolved tenant {} from domain {}", tenant.get().getId(), tenantDomainFromClaims);
                return tenant.get().getId();
            }
        }
        
        throw new TenantResolutionException("Unable to resolve tenant from claims or host");
    }
    
    /**
     * Token validation interface.
     */
    public interface TokenValidator {
        /**
         * Validates a JWT token and extracts claims.
         * 
         * @param authorizationHeader the Authorization header value
         * @return the token claims
         * @throws TokenValidationException if token is invalid
         */
        TokenClaims validate(String authorizationHeader);
    }
    
    /**
     * Token claims record.
     */
    public record TokenClaims(String tenantId, String tenantDomain) {}
    
    /**
     * Tenant entity.
     */
    public record Tenant(String id, String domain) {}
    
    /**
     * Repository interface for tenant lookup.
     */
    public interface TenantRepository {
        Optional<Tenant> findById(String id);
        Optional<Tenant> findByDomain(String domain);
    }
    
    /**
     * Exception thrown when tenant cannot be resolved.
     */
    public static class TenantResolutionException extends RuntimeException {
        public TenantResolutionException(String message) {
            super(message);
        }
    }
    
    /**
     * Exception thrown when token validation fails.
     */
    public static class TokenValidationException extends RuntimeException {
        public TokenValidationException(String message) {
            super(message);
        }
        
        public TokenValidationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}

