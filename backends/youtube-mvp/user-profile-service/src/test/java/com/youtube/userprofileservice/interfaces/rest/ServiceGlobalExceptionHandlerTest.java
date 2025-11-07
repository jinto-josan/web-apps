package com.youtube.userprofileservice.interfaces.rest;

import com.youtube.common.domain.services.correlation.CorrelationContext;
import com.youtube.userprofileservice.application.saga.SagaExecutionException;
import com.youtube.userprofileservice.application.saga.SagaStepException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for ServiceGlobalExceptionHandler.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ServiceGlobalExceptionHandler Tests")
class ServiceGlobalExceptionHandlerTest {

    @InjectMocks
    private ServiceGlobalExceptionHandler exceptionHandler;

    private static final String CORRELATION_ID = "test-correlation-id";

    @BeforeEach
    void setUp() {
        CorrelationContext.setCorrelationId(CORRELATION_ID);
    }

    @Test
    @DisplayName("Should handle SagaExecutionException")
    void shouldHandleSagaExecutionException() {
        // Given
        SagaExecutionException ex = new SagaExecutionException(
                "saga-123",
                "UPDATE_PROFILE",
                "LOAD_PROFILE",
                "Saga execution failed",
                new RuntimeException("Underlying error")
        );

        // When
        ProblemDetail problem = exceptionHandler.handleSagaExecutionException(ex);

        // Then
        assertThat(problem).isNotNull();
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR.value());
        assertThat(problem.getTitle()).isEqualTo("Saga Execution Failed");
        assertThat(problem.getProperties().get("sagaId")).isEqualTo("saga-123");
        assertThat(problem.getProperties().get("sagaType")).isEqualTo("UPDATE_PROFILE");
        assertThat(problem.getProperties().get("failedStep")).isEqualTo("LOAD_PROFILE");
        assertThat(problem.getProperties().get("correlationId")).isEqualTo(CORRELATION_ID);
    }

    @Test
    @DisplayName("Should handle SagaStepException with PROFILE_NOT_FOUND")
    void shouldHandleSagaStepExceptionWithProfileNotFound() {
        // Given
        SagaStepException ex = new SagaStepException(
                "LOAD_PROFILE",
                "saga-123",
                "PROFILE_NOT_FOUND"
        );

        // When
        ProblemDetail problem = exceptionHandler.handleSagaStepException(ex);

        // Then
        assertThat(problem).isNotNull();
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.NOT_FOUND.value());
        assertThat(problem.getTitle()).isEqualTo("Saga Step Failed");
        assertThat(problem.getProperties().get("errorCode")).isEqualTo("PROFILE_NOT_FOUND");
        assertThat(problem.getProperties().get("correlationId")).isEqualTo(CORRELATION_ID);
    }

    @Test
    @DisplayName("Should handle SagaStepException with ETAG_MISMATCH")
    void shouldHandleSagaStepExceptionWithETagMismatch() {
        // Given
        SagaStepException ex = new SagaStepException(
                "UPDATE_PROFILE",
                "saga-123",
                "ETAG_MISMATCH"
        );

        // When
        ProblemDetail problem = exceptionHandler.handleSagaStepException(ex);

        // Then
        assertThat(problem).isNotNull();
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.CONFLICT.value());
        assertThat(problem.getProperties().get("errorCode")).isEqualTo("ETAG_MISMATCH");
    }

    @Test
    @DisplayName("Should handle SagaStepException with VALIDATION_FAILED")
    void shouldHandleSagaStepExceptionWithValidationFailed() {
        // Given
        SagaStepException ex = new SagaStepException(
                "VALIDATE_PHOTO_URL",
                "saga-123",
                "VALIDATION_FAILED"
        );

        // When
        ProblemDetail problem = exceptionHandler.handleSagaStepException(ex);

        // Then
        assertThat(problem).isNotNull();
        assertThat(problem.getStatus()).isEqualTo(HttpStatus.BAD_REQUEST.value());
        assertThat(problem.getProperties().get("errorCode")).isEqualTo("VALIDATION_FAILED");
    }
}

