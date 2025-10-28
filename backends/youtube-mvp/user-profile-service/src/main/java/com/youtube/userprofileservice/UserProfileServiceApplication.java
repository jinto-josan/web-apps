package com.youtube.userprofileservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Main application class for User Profile Service.
 */
@SpringBootApplication(scanBasePackages = {
    "com.youtube.userprofileservice",
    "com.youtube.common"
})
@EntityScan(basePackages = "com.youtube.userprofileservice.infrastructure.persistence.entity")
@EnableJpaRepositories(basePackages = "com.youtube.userprofileservice.infrastructure.persistence.repository")
@EnableCaching
public class UserProfileServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(UserProfileServiceApplication.class, args);
    }
}

