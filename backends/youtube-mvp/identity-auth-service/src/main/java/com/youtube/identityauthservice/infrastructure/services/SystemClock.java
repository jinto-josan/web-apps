package com.youtube.identityauthservice.infrastructure.services;

import com.youtube.common.domain.core.Clock;
import org.springframework.stereotype.Component;

import java.time.Instant;

/**
 * System clock implementation using common-domain Clock interface.
 */
@Component
public class SystemClock implements Clock {
    
    @Override
    public Instant now() {
        return Instant.now();
    }
}

