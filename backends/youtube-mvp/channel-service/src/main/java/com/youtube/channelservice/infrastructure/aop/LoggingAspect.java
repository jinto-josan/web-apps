package com.youtube.channelservice.infrastructure.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/**
 * Aspect for logging method execution times and parameters.
 * Provides cross-cutting logging functionality for service methods.
 */
@Aspect
@Component
@Slf4j
public class LoggingAspect {
    
    /**
     * Logs method execution for all service methods.
     * @param joinPoint The join point
     * @return The method result
     * @throws Throwable If the method throws an exception
     */
    @Around("execution(* com.youtube.channelservice.application..*.*(..)) || " +
            "execution(* com.youtube.channelservice.domain.services..*.*(..))")
    public Object logMethodExecution(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        Object[] args = joinPoint.getArgs();
        
        log.info("Executing {}.{} with args: {}", className, methodName, args);
        
        Instant start = Instant.now();
        try {
            Object result = joinPoint.proceed();
            Duration duration = Duration.between(start, Instant.now());
            log.info("Completed {}.{} in {}ms with result: {}", 
                    className, methodName, duration.toMillis(), result);
            return result;
        } catch (Exception e) {
            Duration duration = Duration.between(start, Instant.now());
            log.error("Failed {}.{} in {}ms with error: {}", 
                    className, methodName, duration.toMillis(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Logs repository method calls for database operations.
     * @param joinPoint The join point
     * @return The method result
     * @throws Throwable If the method throws an exception
     */
    @Around("execution(* com.youtube.channelservice.infrastructure.persistence.repository..*.*(..))")
    public Object logRepositoryCalls(ProceedingJoinPoint joinPoint) throws Throwable {
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String methodName = joinPoint.getSignature().getName();
        
        log.debug("Repository call: {}.{}", className, methodName);
        
        Instant start = Instant.now();
        try {
            Object result = joinPoint.proceed();
            Duration duration = Duration.between(start, Instant.now());
            log.debug("Repository call completed: {}.{} in {}ms", 
                    className, methodName, duration.toMillis());
            return result;
        } catch (Exception e) {
            Duration duration = Duration.between(start, Instant.now());
            log.error("Repository call failed: {}.{} in {}ms with error: {}", 
                    className, methodName, duration.toMillis(), e.getMessage(), e);
            throw e;
        }
    }
}
