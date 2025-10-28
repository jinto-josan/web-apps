package com.youtube.mvp.videocatalog.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Enables scheduled tasks for Outbox processor.
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}

