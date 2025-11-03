package com.youtube.common.domain.persistence.idempotency;

import java.util.Optional;

/**
 * Repository interface for HTTP idempotency pattern.
 * 
 * <p>Implementations should provide storage and retrieval of idempotency
 * records keyed by idempotency key and request hash.</p>
 * 
 * <p>This interface allows different storage backends (JPA, Redis, etc.)
 * to be used seamlessly by the IdempotencyFilter.</p>
 */
public interface HttpIdempotencyRepository {
    
    /**
     * Finds a stored response by idempotency key and request hash.
     * 
     * @param key the idempotency key from the request header
     * @param hash the SHA-256 hash of the request (method + URI + body)
     * @return the stored response if found and hash matches, empty otherwise
     */
    Optional<StoredResponse> findByIdempotencyKeyAndRequestHash(String key, byte[] hash);
    
    /**
     * Stores a response for an idempotency key.
     * 
     * @param key the idempotency key
     * @param requestHash the SHA-256 hash of the request
     * @param status the HTTP response status code
     * @param body the response body bytes
     */
    void storeResponse(String key, byte[] requestHash, int status, byte[] body);
    
    /**
     * Record representing a stored HTTP response.
     * 
     * @param status the HTTP status code
     * @param body the response body bytes
     */
    record StoredResponse(int status, byte[] body) {
        /**
         * Checks if this response has a body.
         * 
         * @return true if body is not null and not empty
         */
        public boolean hasBody() {
            return body != null && body.length > 0;
        }
    }
}

