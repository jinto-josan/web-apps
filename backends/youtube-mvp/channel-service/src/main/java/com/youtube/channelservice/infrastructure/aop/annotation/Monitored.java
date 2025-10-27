package com.youtube.channelservice.infrastructure.aop.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for marking methods that should be monitored for performance.
 * Used with AOP to collect metrics and performance data.
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface Monitored {
    
    /**
     * The name of the metric to collect.
     * @return The metric name
     */
    String value() default "";
    
    /**
     * Whether to include method parameters in logging.
     * @return true if parameters should be logged
     */
    boolean includeParameters() default true;
    
    /**
     * Whether to include return values in logging.
     * @return true if return values should be logged
     */
    boolean includeReturnValue() default false;
    
    /**
     * The threshold in milliseconds for slow operation warnings.
     * @return The threshold in milliseconds
     */
    long slowThresholdMs() default 1000;
}
