package com.youtube.common.domain.core;

import java.time.Instant;

/**
 * Interface for getting the current time.
 * Allows for time abstraction in tests and business logic.
 */
public interface Clock {
    /**
     * Returns the current instant.
     * 
     * @return the current instant
     */
    Instant now();
    
    /**
     * Default implementation using system clock.
     */
    class SystemClock implements Clock {
        @Override
        public Instant now() {
            return Instant.now();
        }
    }
}

