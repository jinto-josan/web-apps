package com.youtube.common.domain;

/**
 * Exception thrown when a requested resource is not found.
 */
public class NotFoundException extends DomainException {
    
    public NotFoundException(String message) {
        super("NOT_FOUND", message, false);
    }
    
    public NotFoundException(String resource, String id) {
        super("NOT_FOUND", resource + " with ID '" + id + "' not found", false);
    }
}
