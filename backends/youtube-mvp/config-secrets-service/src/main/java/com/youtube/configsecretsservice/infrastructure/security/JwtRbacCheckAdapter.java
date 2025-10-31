package com.youtube.configsecretsservice.infrastructure.security;

import com.youtube.configsecretsservice.domain.port.RbacCheckPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;

/**
 * Adapter implementing RBAC checks using JWT claims.
 */
@Component
@Slf4j
public class JwtRbacCheckAdapter implements RbacCheckPort {
    
    @Override
    public boolean canRead(String userId, String tenantId, String scope) {
        return hasPermission(userId, tenantId, scope, "config.read", "config.admin");
    }
    
    @Override
    public boolean canWrite(String userId, String tenantId, String scope) {
        return hasPermission(userId, tenantId, scope, "config.write", "config.admin");
    }
    
    @Override
    public boolean canRotateSecret(String userId, String tenantId, String scope) {
        return hasPermission(userId, tenantId, scope, "config.secret.rotate", "config.admin");
    }
    
    private boolean hasPermission(String userId, String tenantId, String scope, String requiredPermission, String adminPermission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt)) {
            return false;
        }
        
        Jwt jwt = (Jwt) authentication.getPrincipal();
        
        // Check tenant access (user's tenant must match scope tenant or be admin)
        String userTenant = jwt.getClaimAsString("tid");
        if (userTenant != null && !userTenant.equals(tenantId) && !hasScope(jwt, adminPermission)) {
            log.debug("Tenant mismatch: user tenant={}, scope tenant={}", userTenant, tenantId);
            return false;
        }
        
        // Check permissions
        if (hasScope(jwt, adminPermission)) {
            return true;
        }
        
        // Check scope-specific permission
        String scopePermission = scope + "." + requiredPermission;
        if (hasScope(jwt, scopePermission)) {
            return true;
        }
        
        // Check global permission
        return hasScope(jwt, requiredPermission);
    }
    
    @SuppressWarnings("unchecked")
    private boolean hasScope(Jwt jwt, String scope) {
        // Check 'scp' claim (array)
        Object scp = jwt.getClaim("scp");
        if (scp instanceof String) {
            return scp.equals(scope) || ((String) scp).contains(scope);
        }
        if (scp instanceof List) {
            return ((List<?>) scp).contains(scope);
        }
        
        // Check 'roles' claim
        Object roles = jwt.getClaim("roles");
        if (roles instanceof List) {
            return ((List<?>) roles).contains(scope);
        }
        
        // Check 'permissions' claim
        Object permissions = jwt.getClaim("permissions");
        if (permissions instanceof List) {
            return ((List<?>) permissions).contains(scope);
        }
        
        return false;
    }
}

