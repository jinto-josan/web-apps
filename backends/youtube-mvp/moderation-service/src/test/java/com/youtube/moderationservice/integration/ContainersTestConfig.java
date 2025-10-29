package com.youtube.moderationservice.integration;

import org.junit.jupiter.api.BeforeAll;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;

public class ContainersTestConfig {
    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>(DockerImageName.parse("postgres:15-alpine"));

    @BeforeAll
    static void start() {
        POSTGRES.start();
        System.setProperty("POSTGRES_URL", POSTGRES.getJdbcUrl());
        System.setProperty("POSTGRES_USER", POSTGRES.getUsername());
        System.setProperty("POSTGRES_PASSWORD", POSTGRES.getPassword());
    }
}


