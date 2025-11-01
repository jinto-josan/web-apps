package com.youtube.userprofileservice.interfaces.rest;

import com.youtube.common.domain.error.GlobalExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for User Profile Service.
 * Extends the common-domain error handler.
 */
@RestControllerAdvice
public class ServiceGlobalExceptionHandler extends GlobalExceptionHandler {
    // Service-specific exception handlers can be added here if needed
}

