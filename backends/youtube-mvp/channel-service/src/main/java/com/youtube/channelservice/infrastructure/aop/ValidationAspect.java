package com.youtube.channelservice.infrastructure.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.Set;

/**
 * Aspect for validation of method parameters and return values.
 * Provides cross-cutting validation functionality using Bean Validation.
 */
@Aspect
@Component
@Slf4j
public class ValidationAspect {
    
    private final Validator validator;
    
    public ValidationAspect(Validator validator) {
        this.validator = validator;
    }
    
    /**
     * Validates command parameters before execution.
     * @param joinPoint The join point
     * @return The method result
     * @throws Throwable If validation fails or the method throws an exception
     */
    @Around("execution(* com.youtube.channelservice.application.commands..*.execute(..))")
    public Object validateCommandParameters(ProceedingJoinPoint joinPoint) throws Throwable {
        String commandType = joinPoint.getTarget().getClass().getSimpleName();
        
        // Validate command object
        Object command = joinPoint.getTarget();
        Set<ConstraintViolation<Object>> violations = validator.validate(command);
        
        if (!violations.isEmpty()) {
            log.error("Validation failed for command {}: {}", commandType, violations);
            throw new IllegalArgumentException("Command validation failed: " + violations);
        }
        
        // Validate method parameters
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg != null) {
                Set<ConstraintViolation<Object>> paramViolations = validator.validate(arg);
                if (!paramViolations.isEmpty()) {
                    log.error("Parameter validation failed for command {}: {}", commandType, paramViolations);
                    throw new IllegalArgumentException("Parameter validation failed: " + paramViolations);
                }
            }
        }
        
        log.debug("Validation passed for command: {}", commandType);
        return joinPoint.proceed();
    }
    
    /**
     * Validates saga parameters before execution.
     * @param joinPoint The join point
     * @return The method result
     * @throws Throwable If validation fails or the method throws an exception
     */
    @Around("execution(* com.youtube.channelservice.application.saga..*.execute(..))")
    public Object validateSagaParameters(ProceedingJoinPoint joinPoint) throws Throwable {
        String sagaType = joinPoint.getTarget().getClass().getSimpleName();
        
        // Validate saga object
        Object saga = joinPoint.getTarget();
        Set<ConstraintViolation<Object>> violations = validator.validate(saga);
        
        if (!violations.isEmpty()) {
            log.error("Validation failed for saga {}: {}", sagaType, violations);
            throw new IllegalArgumentException("Saga validation failed: " + violations);
        }
        
        log.debug("Validation passed for saga: {}", sagaType);
        return joinPoint.proceed();
    }
    
    /**
     * Validates domain model parameters.
     * @param joinPoint The join point
     * @return The method result
     * @throws Throwable If validation fails or the method throws an exception
     */
    @Around("execution(* com.youtube.channelservice.domain.models..*.*(..))")
    public Object validateDomainModels(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getName();
        
        // Validate method parameters
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg != null) {
                Set<ConstraintViolation<Object>> violations = validator.validate(arg);
                if (!violations.isEmpty()) {
                    log.error("Domain model validation failed for method {}: {}", methodName, violations);
                    throw new IllegalArgumentException("Domain model validation failed: " + violations);
                }
            }
        }
        
        Object result = joinPoint.proceed();
        
        // Validate return value
        if (result != null) {
            Set<ConstraintViolation<Object>> violations = validator.validate(result);
            if (!violations.isEmpty()) {
                log.error("Return value validation failed for method {}: {}", methodName, violations);
                throw new IllegalArgumentException("Return value validation failed: " + violations);
            }
        }
        
        return result;
    }
}
