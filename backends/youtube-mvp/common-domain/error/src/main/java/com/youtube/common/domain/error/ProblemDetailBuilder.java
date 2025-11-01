package com.youtube.common.domain.error;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import java.util.List;
import java.util.Map;

/**
 * Builder utility for creating RFC 7807 Problem Details.
 * 
 * <p>Provides a fluent API for constructing ProblemDetail objects
 * with consistent structure across services.</p>
 */
public final class ProblemDetailBuilder {
    
    private ProblemDetailBuilder() {
        // Utility class
    }
    
    /**
     * Creates a problem detail for a domain exception.
     * 
     * @param exception the domain exception
     * @param status the HTTP status code
     * @return the problem detail
     */
    public static ProblemDetail fromDomainException(DomainException exception, HttpStatus status) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(getTitleForErrorCode(exception.getErrorCode()));
        problemDetail.setDetail(exception.getMessage());
        problemDetail.setProperty("errorCode", exception.getErrorCode());
        
        // Add validation errors if available
        if (exception instanceof ValidationException ve && ve.hasValidationErrors()) {
            problemDetail.setProperty("validationErrors", ve.getValidationErrors());
        }
        
        // Add resource info if available
        if (exception instanceof NotFoundException nfe) {
            problemDetail.setProperty("resourceType", nfe.getResourceType());
            problemDetail.setProperty("resourceId", nfe.getResourceId());
        }
        
        return problemDetail;
    }
    
    /**
     * Creates a problem detail for validation errors.
     * 
     * @param message the error message
     * @param validationErrors map of field names to error messages
     * @return the problem detail
     */
    public static ProblemDetail validationError(String message, Map<String, List<String>> validationErrors) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.BAD_REQUEST);
        problemDetail.setTitle("Validation Failed");
        problemDetail.setDetail(message);
        problemDetail.setProperty("errorCode", ErrorCodes.VALIDATION_FAILED);
        if (validationErrors != null && !validationErrors.isEmpty()) {
            problemDetail.setProperty("validationErrors", validationErrors);
        }
        return problemDetail;
    }
    
    /**
     * Creates a problem detail for not found errors.
     * 
     * @param resourceType the type of resource
     * @param resourceId the resource identifier
     * @return the problem detail
     */
    public static ProblemDetail notFound(String resourceType, String resourceId) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.NOT_FOUND);
        problemDetail.setTitle("Resource Not Found");
        problemDetail.setDetail(String.format("%s with id '%s' not found", resourceType, resourceId));
        problemDetail.setProperty("errorCode", ErrorCodes.RESOURCE_NOT_FOUND);
        problemDetail.setProperty("resourceType", resourceType);
        problemDetail.setProperty("resourceId", resourceId);
        return problemDetail;
    }
    
    /**
     * Creates a problem detail for conflict errors.
     * 
     * @param message the error message
     * @return the problem detail
     */
    public static ProblemDetail conflict(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.CONFLICT);
        problemDetail.setTitle("Conflict");
        problemDetail.setDetail(message);
        problemDetail.setProperty("errorCode", ErrorCodes.CONFLICT);
        return problemDetail;
    }
    
    /**
     * Creates a problem detail for unauthorized errors.
     * 
     * @param message the error message
     * @return the problem detail
     */
    public static ProblemDetail unauthorized(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.UNAUTHORIZED);
        problemDetail.setTitle("Unauthorized");
        problemDetail.setDetail(message);
        problemDetail.setProperty("errorCode", ErrorCodes.UNAUTHORIZED);
        return problemDetail;
    }
    
    /**
     * Creates a problem detail for forbidden errors.
     * 
     * @param message the error message
     * @return the problem detail
     */
    public static ProblemDetail forbidden(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.FORBIDDEN);
        problemDetail.setTitle("Forbidden");
        problemDetail.setDetail(message);
        problemDetail.setProperty("errorCode", ErrorCodes.FORBIDDEN);
        return problemDetail;
    }
    
    /**
     * Creates a problem detail for internal server errors.
     * 
     * @param message the error message (optional, can be null)
     * @return the problem detail
     */
    public static ProblemDetail internalError(String message) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setDetail(message != null ? message : "An unexpected error occurred");
        problemDetail.setProperty("errorCode", ErrorCodes.INTERNAL_ERROR);
        return problemDetail;
    }
    
    /**
     * Creates a problem detail with custom properties.
     * 
     * @param status the HTTP status
     * @param title the problem title
     * @param detail the problem detail
     * @param errorCode the error code
     * @param properties additional properties
     * @return the problem detail
     */
    public static ProblemDetail custom(
            HttpStatus status,
            String title,
            String detail,
            String errorCode,
            Map<String, Object> properties) {
        ProblemDetail problemDetail = ProblemDetail.forStatus(status);
        problemDetail.setTitle(title);
        problemDetail.setDetail(detail);
        problemDetail.setProperty("errorCode", errorCode);
        
        if (properties != null) {
            properties.forEach(problemDetail::setProperty);
        }
        
        return problemDetail;
    }
    
    private static String getTitleForErrorCode(String errorCode) {
        return switch (errorCode) {
            case ErrorCodes.VALIDATION_FAILED -> "Validation Failed";
            case ErrorCodes.RESOURCE_NOT_FOUND -> "Resource Not Found";
            case ErrorCodes.CONFLICT -> "Conflict";
            case ErrorCodes.CONCURRENCY_CONFLICT -> "Concurrency Conflict";
            case ErrorCodes.VERSION_CONFLICT -> "Version Conflict";
            case ErrorCodes.UNAUTHORIZED -> "Unauthorized";
            case ErrorCodes.FORBIDDEN -> "Forbidden";
            case ErrorCodes.BAD_REQUEST -> "Bad Request";
            case ErrorCodes.IDEMPOTENCY_KEY_MISMATCH -> "Idempotency Key Mismatch";
            case ErrorCodes.EVENT_PROCESSING_FAILED -> "Event Processing Failed";
            case ErrorCodes.EVENT_DESERIALIZATION_FAILED -> "Event Deserialization Failed";
            case ErrorCodes.SERVICE_UNAVAILABLE -> "Service Unavailable";
            case ErrorCodes.TIMEOUT -> "Timeout";
            default -> "Error";
        };
    }
}

