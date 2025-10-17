package com.youtube.common.domain;

import java.time.Instant;

/**
 * Implementation of Clock that uses the system clock.
 */
public class SystemClock implements Clock {
    
    @Override
    public Instant now() {
        return Instant.now();
    }
}
