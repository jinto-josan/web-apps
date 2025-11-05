package com.youtube.identityauthservice;

import com.microsoft.applicationinsights.attach.ApplicationInsights;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Main Spring Boot application class for Identity-Auth Service.
 * This service handles authentication and authorization for the YouTube MVP platform,
 * including local login, Azure AD B2C integration, device flow, MFA, and more.
 */
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
public class IdentityAuthApplication {
    
    private static final Logger log = LoggerFactory.getLogger(IdentityAuthApplication.class);
    
    public static void main(String[] args) {
        log.info("Starting Identity Auth Service...");
        
        // Attach Application Insights agent for auto-instrumentation
        // This must be called before SpringApplication.run()
        try {
            ApplicationInsights.attach();
            log.info("Application Insights agent attached successfully");
        } catch (Exception e) {
            log.warn("Failed to attach Application Insights agent: {}", e.getMessage());
        }
        
        try {
            SpringApplication.run(IdentityAuthApplication.class, args);
            log.info("Identity Auth Service started successfully");
        } catch (Exception e) {
            log.error("Failed to start Identity Auth Service", e);
            throw e;
        }
    }
}