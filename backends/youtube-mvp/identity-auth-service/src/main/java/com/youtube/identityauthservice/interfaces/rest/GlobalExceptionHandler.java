package com.youtube.identityauthservice.interfaces.rest;

import com.youtube.common.domain.error.ProblemDetailBuilder;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for Identity Auth Service.
 * Extends the common-domain error handler and adds service-specific handlers.
 */
@RestControllerAdvice
public class GlobalExceptionHandler extends com.youtube.common.domain.error.GlobalExceptionHandler {
    
    /**
     * Handles security exceptions specific to authentication/authorization.
     */
    @ExceptionHandler(SecurityException.class)
    public ProblemDetail handleSecurity(SecurityException ex) {
        return ProblemDetailBuilder.unauthorized(ex.getMessage());
    }
}