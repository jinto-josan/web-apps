package com.youtube.channelservice.infrastructure.aop;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Aspect for collecting metrics on method execution.
 * Provides cross-cutting metrics functionality for monitoring.
 */
@Aspect
@Component
@Slf4j
public class MetricsAspect {
    
    private final AtomicLong commandExecutionCount = new AtomicLong(0);
    private final AtomicLong sagaExecutionCount = new AtomicLong(0);
    private final AtomicLong totalCommandExecutionTime = new AtomicLong(0);
    private final AtomicLong totalSagaExecutionTime = new AtomicLong(0);
    
    /**
     * Collects metrics for command executions.
     * @param joinPoint The join point
     * @return The method result
     * @throws Throwable If the method throws an exception
     */
    @Around("execution(* com.youtube.channelservice.application.commands..*.execute(..))")
    public Object collectCommandMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        String commandType = joinPoint.getTarget().getClass().getSimpleName();
        
        Instant start = Instant.now();
        boolean success = false;
        try {
            Object result = joinPoint.proceed();
            success = true;
            return result;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            long durationMs = duration.toMillis();
            
            commandExecutionCount.incrementAndGet();
            totalCommandExecutionTime.addAndGet(durationMs);
            
            log.info("Command Metrics - Type: {}, Duration: {}ms, Success: {}, Total Executions: {}, Avg Duration: {}ms",
                    commandType, durationMs, success, commandExecutionCount.get(), 
                    getAverageCommandExecutionTime());
        }
    }
    
    /**
     * Collects metrics for saga executions.
     * @param joinPoint The join point
     * @return The method result
     * @throws Throwable If the method throws an exception
     */
    @Around("execution(* com.youtube.channelservice.application.saga..*.execute(..))")
    public Object collectSagaMetrics(ProceedingJoinPoint joinPoint) throws Throwable {
        String sagaType = joinPoint.getTarget().getClass().getSimpleName();
        
        Instant start = Instant.now();
        boolean success = false;
        try {
            Object result = joinPoint.proceed();
            success = true;
            return result;
        } finally {
            Duration duration = Duration.between(start, Instant.now());
            long durationMs = duration.toMillis();
            
            sagaExecutionCount.incrementAndGet();
            totalSagaExecutionTime.addAndGet(durationMs);
            
            log.info("Saga Metrics - Type: {}, Duration: {}ms, Success: {}, Total Executions: {}, Avg Duration: {}ms",
                    sagaType, durationMs, success, sagaExecutionCount.get(), 
                    getAverageSagaExecutionTime());
        }
    }
    
    /**
     * Gets the average command execution time.
     * @return Average execution time in milliseconds
     */
    public double getAverageCommandExecutionTime() {
        long count = commandExecutionCount.get();
        return count > 0 ? (double) totalCommandExecutionTime.get() / count : 0.0;
    }
    
    /**
     * Gets the average saga execution time.
     * @return Average execution time in milliseconds
     */
    public double getAverageSagaExecutionTime() {
        long count = sagaExecutionCount.get();
        return count > 0 ? (double) totalSagaExecutionTime.get() / count : 0.0;
    }
    
    /**
     * Gets the total command execution count.
     * @return Total number of command executions
     */
    public long getCommandExecutionCount() {
        return commandExecutionCount.get();
    }
    
    /**
     * Gets the total saga execution count.
     * @return Total number of saga executions
     */
    public long getSagaExecutionCount() {
        return sagaExecutionCount.get();
    }
}
