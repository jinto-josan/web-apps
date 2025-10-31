package com.youtube.configsecretsservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

/**
 * Main application class for Configuration and Secrets Service.
 */
@SpringBootApplication
@EnableAsync
public class ConfigSecretsServiceApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(ConfigSecretsServiceApplication.class, args);
    }
}

