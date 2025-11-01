package com.youtube.common.domain.services.tenant;

import java.util.Optional;

/**
 * Thread-local request context for storing tenant and other request-scoped information.
 */
public class RequestContext {
    
    private static final ThreadLocal<Context> context = new ThreadLocal<>();
    
    /**
     * Sets the tenant ID in the current request context.
     * 
     * @param tenantId the tenant ID
     */
    public static void setTenantId(String tenantId) {
        Context current = context.get();
        if (current == null) {
            context.set(new Context(tenantId, null, null));
        } else {
            context.set(new Context(tenantId, current.userId(), current.correlationId()));
        }
    }
    
    /**
     * Gets the tenant ID from the current request context.
     * 
     * @return the tenant ID, or empty if not set
     */
    public static Optional<String> getTenantId() {
        Context current = context.get();
        return current != null ? Optional.ofNullable(current.tenantId()) : Optional.empty();
    }
    
    /**
     * Sets the user ID in the current request context.
     * 
     * @param userId the user ID
     */
    public static void setUserId(String userId) {
        Context current = context.get();
        if (current == null) {
            context.set(new Context(null, userId, null));
        } else {
            context.set(new Context(current.tenantId(), userId, current.correlationId()));
        }
    }
    
    /**
     * Gets the user ID from the current request context.
     * 
     * @return the user ID, or empty if not set
     */
    public static Optional<String> getUserId() {
        Context current = context.get();
        return current != null ? Optional.ofNullable(current.userId()) : Optional.empty();
    }
    
    /**
     * Sets the correlation ID in the current request context.
     * 
     * @param correlationId the correlation ID
     */
    public static void setCorrelationId(String correlationId) {
        Context current = context.get();
        if (current == null) {
            context.set(new Context(null, null, correlationId));
        } else {
            context.set(new Context(current.tenantId(), current.userId(), correlationId));
        }
    }
    
    /**
     * Gets the correlation ID from the current request context.
     * 
     * @return the correlation ID, or empty if not set
     */
    public static Optional<String> getCorrelationId() {
        Context current = context.get();
        return current != null ? Optional.ofNullable(current.correlationId()) : Optional.empty();
    }
    
    /**
     * Clears the request context for the current thread.
     */
    public static void clear() {
        context.remove();
    }
    
    /**
     * Gets the full context.
     * 
     * @return the context, or empty if not set
     */
    public static Optional<Context> getContext() {
        return Optional.ofNullable(context.get());
    }
    
    /**
     * Request context record.
     */
    public record Context(String tenantId, String userId, String correlationId) {}
}

