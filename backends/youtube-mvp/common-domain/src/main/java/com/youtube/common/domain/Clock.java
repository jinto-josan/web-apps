package com.youtube.common.domain;

import java.time.Instant;

/**
 * Interface for getting the current time.
 * Allows for time mocking in tests and time zone handling.
 */
public interface Clock {
    
    /**
     * Gets the current instant in time.
     * 
     * @return the current instant
     */
    Instant now();
}
