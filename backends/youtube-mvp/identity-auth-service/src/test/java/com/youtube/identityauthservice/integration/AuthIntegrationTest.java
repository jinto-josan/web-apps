package com.youtube.identityauthservice.integration;

import com.youtube.common.domain.shared.valueobjects.UserId;
import com.youtube.identityauthservice.application.queries.GetUserQuery;
import com.youtube.identityauthservice.application.usecases.AuthUseCase;
import com.youtube.identityauthservice.domain.entities.User;
import com.youtube.identityauthservice.domain.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;


import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@DisplayName("Authentication Integration Tests")
class AuthIntegrationTest {

    @Container
    @SuppressWarnings("resource")
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("app.access-token-ttl-seconds", () -> "3600");
        registry.add("app.refresh-token-ttl-seconds", () -> "86400");
        registry.add("app.oidc.enabled", () -> "false");
        registry.add("azure.servicebus.enabled", () -> "false");
        registry.add("azure.appconfig.enabled", () -> "false");
        registry.add("azure.keyvault.enabled", () -> "false");
    }

    @Autowired
    private AuthUseCase authUseCase;

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should get user by ID")
    void shouldGetUserById() {
        // Given
        UserId userId = UserId.from("test-user-123");
        User user = User.builder()
                .id(userId)
                .email("integration@example.com")
                .normalizedEmail("integration@example.com")
                .displayName("Integration Test User")
                .emailVerified(true)
                .status((short) 1)
                .createdAt(java.time.Instant.now())
                .updatedAt(java.time.Instant.now())
                .version(0)
                .build();

        userRepository.save(user);

        GetUserQuery query = GetUserQuery.builder()
                .userId(userId.asString())
                .build();

        // When
        User result = authUseCase.getUser(query);

        // Then
        assertNotNull(result);
        assertEquals(userId, result.getId());
        assertEquals("integration@example.com", result.getEmail());
    }

    @Test
    @DisplayName("Should throw exception when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
        // Given
        GetUserQuery query = GetUserQuery.builder()
                .userId("nonexistent-user-id")
                .build();

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> authUseCase.getUser(query));
    }
}

