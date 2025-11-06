package com.youtube.common.domain.error;

import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.ErrorResponseException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Base global exception handler for REST controllers.
 * Provides consistent error handling across all services using RFC 7807 Problem Details.
 * 
 * <p>Services can extend this class and add their own exception handlers if needed:</p>
 * 
 * <pre>{@code
 * @RestControllerAdvice
 * public class ServiceGlobalExceptionHandler extends GlobalExceptionHandler {
 *     @ExceptionHandler(ServiceSpecificException.class)
 *     public ProblemDetail handleServiceSpecific(ServiceSpecificException ex) {
 *         return ProblemDetailBuilder.fromDomainException(ex, HttpStatus.BAD_REQUEST);
 *     }
 * }
 * }</pre>
 */
@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    /**
     * Handles domain exceptions.
     */
    @ExceptionHandler(DomainException.class)
    public ProblemDetail handleDomainException(DomainException ex) {
        log.debug("Domain exception: {}", ex.getErrorCode(), ex);
        
        HttpStatus status = determineHttpStatus(ex);
        return ProblemDetailBuilder.fromDomainException(ex, status);
    }
    
    /**
     * Handles validation exceptions.
     */
    @ExceptionHandler(ValidationException.class)
    public ProblemDetail handleValidationException(ValidationException ex) {
        log.debug("Validation exception", ex);
        
        if (ex.hasValidationErrors()) {
            return ProblemDetailBuilder.validationError(ex.getMessage(), ex.getValidationErrors());
        }
        return ProblemDetailBuilder.validationError(ex.getMessage(), null);
    }
    
    /**
     * Handles not found exceptions.
     */
    @ExceptionHandler(NotFoundException.class)
    public ProblemDetail handleNotFoundException(NotFoundException ex) {
        log.debug("Resource not found: {} {}", ex.getResourceType(), ex.getResourceId());
        return ProblemDetailBuilder.notFound(ex.getResourceType(), ex.getResourceId());
    }
    
    /**
     * Handles conflict exceptions (including concurrency conflicts).
     */
    @ExceptionHandler(ConflictException.class)
    public ProblemDetail handleConflictException(ConflictException ex) {
        log.debug("Conflict exception", ex);
        return ProblemDetailBuilder.conflict(ex.getMessage());
    }
    
    /**
     * Handles unauthorized exceptions.
     */
    @ExceptionHandler(UnauthorizedException.class)
    public ProblemDetail handleUnauthorizedException(UnauthorizedException ex) {
        log.debug("Unauthorized exception", ex);
        return ProblemDetailBuilder.unauthorized(ex.getMessage());
    }
    
    /**
     * Handles forbidden exceptions.
     */
    @ExceptionHandler(ForbiddenException.class)
    public ProblemDetail handleForbiddenException(ForbiddenException ex) {
        log.debug("Forbidden exception", ex);
        return ProblemDetailBuilder.forbidden(ex.getMessage());
    }
    
    /**
     * Handles method argument validation exceptions.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleMethodArgumentNotValid(MethodArgumentNotValidException ex) {
        log.debug("Method argument validation failed", ex);
        
        Map<String, List<String>> errors = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .collect(Collectors.groupingBy(
                error -> error.getField(),
                Collectors.mapping(
                    error -> error.getDefaultMessage() != null ? error.getDefaultMessage() : "Invalid value",
                    Collectors.toList()
                )
            ));
        
        return ProblemDetailBuilder.validationError(
            "Validation failed for request", 
            errors
        );
    }
    
    /**
     * Handles constraint violation exceptions (Bean Validation).
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ProblemDetail handleConstraintViolation(ConstraintViolationException ex) {
        log.debug("Constraint violation", ex);
        
        Map<String, List<String>> errors = ex.getConstraintViolations()
            .stream()
            .collect(Collectors.groupingBy(
                violation -> violation.getPropertyPath().toString(),
                Collectors.mapping(
                    violation -> violation.getMessage() != null ? violation.getMessage() : "Invalid value",
                    Collectors.toList()
                )
            ));
        
        return ProblemDetailBuilder.validationError(
            "Validation constraint violated",
            errors
        );
    }
    
    /**
     * Handles illegal argument exceptions.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException ex) {
        log.debug("Illegal argument", ex);
        return ProblemDetailBuilder.fromDomainException(
            new DomainException(ErrorCodes.BAD_REQUEST, ex.getMessage()),
            HttpStatus.BAD_REQUEST
        );
    }
    
    /**
     * Handles illegal state exceptions.
     */
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException ex) {
        log.debug("Illegal state", ex);
        return ProblemDetailBuilder.fromDomainException(
            new DomainException(ErrorCodes.BAD_REQUEST, ex.getMessage()),
            HttpStatus.BAD_REQUEST
        );
    }
    
    /**
     * Handles Spring ErrorResponseException.
     */
    @ExceptionHandler(ErrorResponseException.class)
    public ProblemDetail handleErrorResponse(ErrorResponseException ex) {
        log.debug("Error response exception", ex);
        return ex.getBody();
    }
    
    /**
     * Handles all other exceptions as internal server errors.
     */
    @ExceptionHandler(Exception.class)
    public ProblemDetail handleGeneric(Exception ex) {
        log.error("Unexpected exception", ex);
        return ProblemDetailBuilder.internalError("An unexpected error occurred");
    }
    
    /**
     * Determines the appropriate HTTP status for a domain exception.
     * 
     * @param ex the domain exception
     * @return the HTTP status
     */
    protected HttpStatus determineHttpStatus(DomainException ex) {
        String errorCode = ex.getErrorCode();
        
        return switch (errorCode) {
            case ErrorCodes.VALIDATION_FAILED, ErrorCodes.BAD_REQUEST -> HttpStatus.BAD_REQUEST;
            case ErrorCodes.UNAUTHORIZED -> HttpStatus.UNAUTHORIZED;
            case ErrorCodes.FORBIDDEN -> HttpStatus.FORBIDDEN;
            case ErrorCodes.RESOURCE_NOT_FOUND -> HttpStatus.NOT_FOUND;
            case ErrorCodes.CONFLICT, 
                 ErrorCodes.CONCURRENCY_CONFLICT, 
                 ErrorCodes.VERSION_CONFLICT,
                 ErrorCodes.IDEMPOTENCY_KEY_MISMATCH -> HttpStatus.CONFLICT;
            case ErrorCodes.SERVICE_UNAVAILABLE -> HttpStatus.SERVICE_UNAVAILABLE;
            case ErrorCodes.TIMEOUT -> HttpStatus.REQUEST_TIMEOUT;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}

