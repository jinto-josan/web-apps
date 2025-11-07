package com.youtube.userprofileservice.integration;

import com.youtube.common.domain.events.UserCreatedEvent;
import com.youtube.common.domain.shared.valueobjects.UserId;
import com.youtube.userprofileservice.domain.entities.AccountProfile;
import com.youtube.userprofileservice.domain.repositories.ProfileRepository;
import com.youtube.userprofileservice.infrastructure.messaging.UserCreatedEventHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.transaction.annotation.Transactional;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for event handling.
 * Tests UserCreatedEvent handling with real database.
 */
@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
@Transactional
@DisplayName("Event Handling Integration Tests")
class EventHandlingIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("userprofile_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.flyway.enabled", () -> "false");
        registry.add("spring.security.oauth2.resourceserver.jwt.issuer-uri", () -> "http://localhost:8080");
        registry.add("azure.servicebus.enabled", () -> "false");
        registry.add("azure.appconfig.enabled", () -> "false");
        registry.add("azure.keyvault.enabled", () -> "false");
    }

    @Autowired
    private UserCreatedEventHandler eventHandler;

    @Autowired
    private ProfileRepository profileRepository;

    private static final String USER_ID_1 = "01JXX0X5X5X5X5X5X5X5X5X5X";
    private static final String USER_ID_2 = "01JXX0X5X5X5X5X5X5X5X5X5Y";

    @BeforeEach
    void setUp() {
        // Clean up before each test
        if (profileRepository.exists(USER_ID_1)) {
            profileRepository.delete(USER_ID_1);
        }
        if (profileRepository.exists(USER_ID_2)) {
            profileRepository.delete(USER_ID_2);
        }
    }

    @Test
    @DisplayName("Should create profile from UserCreatedEvent")
    void shouldCreateProfileFromUserCreatedEvent() {
        // Given
        UserCreatedEvent event = new UserCreatedEvent(
                UserId.from(USER_ID_1),
                "user1@example.com",
                "user1"
        );

        // When
        eventHandler.handle(event, "correlation-1");

        // Then
        Optional<AccountProfile> profileOpt = profileRepository.findByAccountId(USER_ID_1);
        assertThat(profileOpt).isPresent();
        
        AccountProfile profile = profileOpt.get();
        assertThat(profile.getAccountId()).isEqualTo(USER_ID_1);
        assertThat(profile.getDisplayName()).isEqualTo("user1");
        assertThat(profile.getVersion()).isEqualTo(1);
        assertThat(profile.getCreatedAt()).isNotNull();
        assertThat(profile.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should handle multiple UserCreatedEvents idempotently")
    void shouldHandleMultipleUserCreatedEventsIdempotently() {
        // Given
        UserCreatedEvent event1 = new UserCreatedEvent(
                UserId.from(USER_ID_1),
                "user1@example.com",
                "user1"
        );

        UserCreatedEvent event2 = new UserCreatedEvent(
                UserId.from(USER_ID_1),
                "user1@example.com",
                "user1"
        );

        // When - handle same event twice
        eventHandler.handle(event1, "correlation-1");
        eventHandler.handle(event2, "correlation-2");

        // Then - should only create one profile
        Optional<AccountProfile> profileOpt = profileRepository.findByAccountId(USER_ID_1);
        assertThat(profileOpt).isPresent();
        assertThat(profileOpt.get().getVersion()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should create profiles for different users")
    void shouldCreateProfilesForDifferentUsers() {
        // Given
        UserCreatedEvent event1 = new UserCreatedEvent(
                UserId.from(USER_ID_1),
                "user1@example.com",
                "user1"
        );

        UserCreatedEvent event2 = new UserCreatedEvent(
                UserId.from(USER_ID_2),
                "user2@example.com",
                "user2"
        );

        // When
        eventHandler.handle(event1, "correlation-1");
        eventHandler.handle(event2, "correlation-2");

        // Then
        Optional<AccountProfile> profile1Opt = profileRepository.findByAccountId(USER_ID_1);
        Optional<AccountProfile> profile2Opt = profileRepository.findByAccountId(USER_ID_2);

        assertThat(profile1Opt).isPresent();
        assertThat(profile2Opt).isPresent();
        assertThat(profile1Opt.get().getDisplayName()).isEqualTo("user1");
        assertThat(profile2Opt.get().getDisplayName()).isEqualTo("user2");
    }

    @Test
    @DisplayName("Should use email prefix when username is null")
    void shouldUseEmailPrefixWhenUsernameIsNull() {
        // Given
        UserCreatedEvent event = new UserCreatedEvent(
                UserId.from(USER_ID_1),
                "testuser@example.com",
                null
        );

        // When
        eventHandler.handle(event, "correlation-1");

        // Then
        Optional<AccountProfile> profileOpt = profileRepository.findByAccountId(USER_ID_1);
        assertThat(profileOpt).isPresent();
        assertThat(profileOpt.get().getDisplayName()).isEqualTo("testuser");
    }

    @Test
    @DisplayName("Should use default name when both username and email are null")
    void shouldUseDefaultNameWhenBothUsernameAndEmailAreNull() {
        // Given
        UserCreatedEvent event = new UserCreatedEvent(
                UserId.from(USER_ID_1),
                null,
                null
        );

        // When
        eventHandler.handle(event, "correlation-1");

        // Then
        Optional<AccountProfile> profileOpt = profileRepository.findByAccountId(USER_ID_1);
        assertThat(profileOpt).isPresent();
        assertThat(profileOpt.get().getDisplayName()).isEqualTo("User");
    }
}

