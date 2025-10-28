package com.youtube.userprofileservice.domain.services;

/**
 * Domain service interface for validating blob storage URIs.
 * Validates that photo URLs point to valid blob storage resources.
 */
public interface BlobUriValidator {
    
    /**
     * Validates a blob storage URI.
     * 
     * @param uri the URI to validate
     * @return true if the URI is valid and points to allowed blob storage
     */
    boolean isValid(String uri);
    
    /**
     * Normalizes a blob storage URI.
     * 
     * @param uri the URI to normalize
     * @return the normalized URI
     * @throws IllegalArgumentException if the URI is invalid
     */
    String normalize(String uri);
}

