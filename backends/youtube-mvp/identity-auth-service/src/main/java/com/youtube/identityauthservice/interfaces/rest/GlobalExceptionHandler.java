package com.youtube.identityauthservice.interfaces.rest;

import com.youtube.common.domain.error.ProblemDetailBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for Identity Auth Service.
 * Extends the common-domain error handler and adds service-specific handlers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends com.youtube.common.domain.error.GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handles security exceptions specific to authentication/authorization.
     */
    @ExceptionHandler(SecurityException.class)
    public ProblemDetail handleSecurity(SecurityException ex) {
        log.warn("Security exception occurred: {}", ex.getMessage(), ex);
        return ProblemDetailBuilder.unauthorized(ex.getMessage());
    }
}