package com.youtube.configsecretsservice.domain.port;

/**
 * Port for RBAC (Role-Based Access Control) checks.
 */
public interface RbacCheckPort {
    boolean canRead(String userId, String tenantId, String scope);
    boolean canWrite(String userId, String tenantId, String scope);
    boolean canRotateSecret(String userId, String tenantId, String scope);
}

