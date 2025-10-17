package com.youtube.common.domain;

import java.util.Set;
import java.util.Objects;

/**
 * Represents the principal (authenticated user) in the system.
 * Contains subject, roles, scopes, and tenant information.
 */
public class Principal {
    private final String subject;
    private final Set<String> roles;
    private final Set<String> scopes;
    private final String tenantId;

    public Principal(String subject, Set<String> roles, Set<String> scopes, String tenantId) {
        this.subject = Objects.requireNonNull(subject, "Subject cannot be null");
        this.roles = Objects.requireNonNull(roles, "Roles cannot be null");
        this.scopes = Objects.requireNonNull(scopes, "Scopes cannot be null");
        this.tenantId = tenantId;
    }

    public String getSubject() {
        return subject;
    }

    public Set<String> getRoles() {
        return roles;
    }

    public Set<String> getScopes() {
        return scopes;
    }

    public String getTenantId() {
        return tenantId;
    }

    /**
     * Checks if the principal has a specific role.
     * 
     * @param role the role to check
     * @return true if the principal has the role
     */
    public boolean hasRole(String role) {
        return roles.contains(role);
    }

    /**
     * Checks if the principal has a specific scope.
     * 
     * @param scope the scope to check
     * @return true if the principal has the scope
     */
    public boolean hasScope(String scope) {
        return scopes.contains(scope);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Principal principal = (Principal) o;
        return Objects.equals(subject, principal.subject) &&
               Objects.equals(roles, principal.roles) &&
               Objects.equals(scopes, principal.scopes) &&
               Objects.equals(tenantId, principal.tenantId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(subject, roles, scopes, tenantId);
    }

    @Override
    public String toString() {
        return "Principal{subject='" + subject + "', roles=" + roles + 
               ", scopes=" + scopes + ", tenantId='" + tenantId + "'}";
    }
}
