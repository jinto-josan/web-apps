package com.youtube.identityauthservice.domain.entities;

/**
 * User type enumeration.
 * Distinguishes between regular users and service principals.
 */
public enum UserType {
    /**
     * Regular user authenticated via email.
     */
    USER,
    
    /**
     * Service principal (application identity).
     */
    SERVICE_PRINCIPAL
}

