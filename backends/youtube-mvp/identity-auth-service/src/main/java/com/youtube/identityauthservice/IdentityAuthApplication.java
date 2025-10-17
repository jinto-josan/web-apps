package com.youtube.identityauthservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
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
    public static void main(String[] args) {
        SpringApplication.run(IdentityAuthApplication.class, args);
    }
}