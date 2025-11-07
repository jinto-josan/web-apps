package com.youtube.userprofileservice.interfaces.rest;

import com.youtube.common.domain.error.GlobalExceptionHandler;
import com.youtube.common.domain.error.ProblemDetailBuilder;
import com.youtube.common.domain.services.correlation.CorrelationContext;
import com.youtube.userprofileservice.application.saga.SagaExecutionException;
import com.youtube.userprofileservice.application.saga.SagaStepException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Global exception handler for User Profile Service.
 * Extends the common-domain error handler and adds service-specific exception handlers.
 */
@Slf4j
@RestControllerAdvice
public class ServiceGlobalExceptionHandler extends GlobalExceptionHandler {
    
    /**
     * Handles saga execution exceptions.
     * These occur when a saga fails during execution.
     */
    @ExceptionHandler(SagaExecutionException.class)
    public ProblemDetail handleSagaExecutionException(SagaExecutionException ex) {
        String correlationId = CorrelationContext.getCorrelationId().orElse("unknown");
        log.warn("Saga execution failed - sagaId: {}, sagaType: {}, failedStep: {}, correlationId: {}", 
                ex.getSagaId(), ex.getSagaType(), ex.getFailedStep(), correlationId, ex);
        
        ProblemDetail problem = ProblemDetailBuilder.custom(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Saga Execution Failed",
                ex.getMessage(),
                "SAGA_EXECUTION_FAILED",
                Map.of(
                        "sagaId", ex.getSagaId(),
                        "sagaType", ex.getSagaType(),
                        "failedStep", ex.getFailedStep(),
                        "correlationId", correlationId
                )
        );
        
        return problem;
    }
    
    /**
     * Handles saga step exceptions.
     * These occur when a specific step within a saga fails.
     */
    @ExceptionHandler(SagaStepException.class)
    public ProblemDetail handleSagaStepException(SagaStepException ex) {
        String correlationId = CorrelationContext.getCorrelationId().orElse("unknown");
        log.warn("Saga step failed - stepName: {}, sagaId: {}, errorCode: {}, correlationId: {}", 
                ex.getStepName(), ex.getSagaId(), ex.getErrorCode(), correlationId, ex);
        
        HttpStatus status = determineStatusFromErrorCode(ex.getErrorCode());
        
        ProblemDetail problem = ProblemDetailBuilder.custom(
                status,
                "Saga Step Failed",
                ex.getMessage(),
                ex.getErrorCode(),
                Map.of(
                        "stepName", ex.getStepName(),
                        "sagaId", ex.getSagaId(),
                        "errorCode", ex.getErrorCode(),
                        "correlationId", correlationId
                )
        );
        
        return problem;
    }
    
    /**
     * Determines HTTP status from saga error code.
     */
    private HttpStatus determineStatusFromErrorCode(String errorCode) {
        return switch (errorCode) {
            case "PROFILE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            case "ETAG_MISMATCH", "VERSION_CONFLICT" -> HttpStatus.CONFLICT;
            case "VALIDATION_FAILED" -> HttpStatus.BAD_REQUEST;
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}

