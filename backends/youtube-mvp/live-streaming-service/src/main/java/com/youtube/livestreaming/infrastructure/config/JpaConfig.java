package com.youtube.livestreaming.infrastructure.config;

import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableTransactionManagement
@EntityScan(basePackages = "com.youtube.livestreaming.infrastructure.persistence.entity")
@EnableJpaRepositories(basePackages = "com.youtube.livestreaming.infrastructure.persistence.repository")
public class JpaConfig {
    // JPA configuration is handled by application.yml
}

