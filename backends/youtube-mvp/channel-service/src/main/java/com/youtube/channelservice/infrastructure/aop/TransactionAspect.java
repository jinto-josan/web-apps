package com.youtube.channelservice.infrastructure.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;

/**
 * Aspect for transaction management and retry logic.
 * Provides cross-cutting transaction functionality for saga operations.
 */
@Aspect
@Component
@Slf4j
public class TransactionAspect {
    
    private static final int MAX_RETRY_ATTEMPTS = 3;
    private static final long RETRY_DELAY_MS = 100;
    
    /**
     * Manages transactions for saga executions with retry logic.
     * @param joinPoint The join point
     * @return The method result
     * @throws Throwable If the method throws an exception
     */
    @Around("execution(* com.youtube.channelservice.application.saga..*.execute(..))")
    @Transactional(rollbackFor = Exception.class)
    public Object manageSagaTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        String sagaType = joinPoint.getTarget().getClass().getSimpleName();
        
        Exception lastException = null;
        for (int attempt = 1; attempt <= MAX_RETRY_ATTEMPTS; attempt++) {
            try {
                log.debug("Executing saga {} (attempt {})", sagaType, attempt);
                Instant start = Instant.now();
                
                Object result = joinPoint.proceed();
                
                Duration duration = Duration.between(start, Instant.now());
                log.info("Saga {} completed successfully in {}ms (attempt {})", 
                        sagaType, duration.toMillis(), attempt);
                
                return result;
                
            } catch (Exception e) {
                lastException = e;
                log.warn("Saga {} failed on attempt {}: {}", sagaType, attempt, e.getMessage());
                
                if (attempt < MAX_RETRY_ATTEMPTS) {
                    try {
                        Thread.sleep(RETRY_DELAY_MS * attempt); // Exponential backoff
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Saga execution interrupted", ie);
                    }
                }
            }
        }
        
        log.error("Saga {} failed after {} attempts", sagaType, MAX_RETRY_ATTEMPTS);
        throw lastException;
    }
    
    /**
     * Manages transactions for command executions.
     * @param joinPoint The join point
     * @return The method result
     * @throws Throwable If the method throws an exception
     */
    @Around("execution(* com.youtube.channelservice.application.commands..*.execute(..))")
    @Transactional(rollbackFor = Exception.class)
    public Object manageCommandTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        String commandType = joinPoint.getTarget().getClass().getSimpleName();
        
        log.debug("Executing command {}", commandType);
        Instant start = Instant.now();
        
        try {
            Object result = joinPoint.proceed();
            
            Duration duration = Duration.between(start, Instant.now());
            log.info("Command {} completed successfully in {}ms", 
                    commandType, duration.toMillis());
            
            return result;
            
        } catch (Exception e) {
            Duration duration = Duration.between(start, Instant.now());
            log.error("Command {} failed in {}ms: {}", 
                    commandType, duration.toMillis(), e.getMessage());
            throw e;
        }
    }
    
    /**
     * Manages read-only transactions for repository operations.
     * @param joinPoint The join point
     * @return The method result
     * @throws Throwable If the method throws an exception
     */
    @Around("execution(* com.youtube.channelservice.infrastructure.persistence.repository..*.find*(..)) || " +
            "execution(* com.youtube.channelservice.infrastructure.persistence.repository..*.exists*(..)) || " +
            "execution(* com.youtube.channelservice.infrastructure.persistence.repository..*.count*(..))")
    @Transactional(readOnly = true)
    public Object manageReadOnlyTransaction(ProceedingJoinPoint joinPoint) throws Throwable {
        return joinPoint.proceed();
    }
}
