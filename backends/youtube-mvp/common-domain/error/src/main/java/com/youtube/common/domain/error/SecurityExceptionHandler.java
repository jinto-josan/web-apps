package com.youtube.common.domain.error;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.http.ProblemDetail;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Spring Security exception handler.
 * Only active when Spring Security is on the classpath.
 * 
 * <p>This handler is conditionally loaded - services without Spring Security
 * will not have this handler registered.</p>
 */
@RestControllerAdvice
@ConditionalOnClass(AccessDeniedException.class)
public class SecurityExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(SecurityExceptionHandler.class);
    
    /**
     * Handles Spring Security access denied exceptions.
     */
    @ExceptionHandler(AccessDeniedException.class)
    public ProblemDetail handleAccessDenied(AccessDeniedException ex) {
        log.debug("Access denied", ex);
        return ProblemDetailBuilder.forbidden(ex.getMessage());
    }
}

