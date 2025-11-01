package com.youtube.common.domain.error;

/**
 * Exception thrown when a requested resource is not found.
 */
public class NotFoundException extends DomainException {
    
    private final String resourceType;
    private final String resourceId;
    
    public NotFoundException(String resourceType, String resourceId) {
        super("RESOURCE_NOT_FOUND", 
            String.format("%s with id '%s' not found", resourceType, resourceId));
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    public NotFoundException(String resourceType, String resourceId, String message) {
        super("RESOURCE_NOT_FOUND", message);
        this.resourceType = resourceType;
        this.resourceId = resourceId;
    }
    
    public String getResourceType() {
        return resourceType;
    }
    
    public String getResourceId() {
        return resourceId;
    }
}

