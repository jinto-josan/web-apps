package com.youtube.userprofileservice.integration;

import com.youtube.common.domain.events.UserCreatedEvent;
import com.youtube.common.domain.shared.valueobjects.UserId;
import com.youtube.common.domain.error.ConflictException;
import com.youtube.userprofileservice.application.commands.*;
import com.youtube.userprofileservice.application.queries.*;
import com.youtube.userprofileservice.application.usecases.ProfileUseCase;
import com.youtube.userprofileservice.domain.entities.*;
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

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for User Profile Service.
 * Tests end-to-end workflows with real database using Testcontainers.
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@Transactional
@DisplayName("Profile Integration Tests")
class ProfileIntegrationTest {

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
        registry.add("app.identity-auth-service.url", () -> "http://localhost:8080");
        registry.add("azure.servicebus.enabled", () -> "false");
        registry.add("azure.appconfig.enabled", () -> "false");
        registry.add("azure.keyvault.enabled", () -> "false");
    }

    @Autowired
    private ProfileUseCase profileUseCase;

    @Autowired
    private ProfileRepository profileRepository;

    @Autowired
    private UserCreatedEventHandler userCreatedEventHandler;

    private static final String ACCOUNT_ID = "01JXX0X5X5X5X5X5X5X5X5X5X";
    private static final String USER_ID = "01JXX0X5X5X5X5X5X5X5X5X5Y";

    @BeforeEach
    void setUp() {
        // Clean up before each test
        if (profileRepository.exists(ACCOUNT_ID)) {
            profileRepository.delete(ACCOUNT_ID);
        }
    }

    @Test
    @DisplayName("Should create profile when UserCreatedEvent is received")
    void shouldCreateProfileWhenUserCreatedEventIsReceived() {
        // Given
        UserCreatedEvent event = new UserCreatedEvent(
                UserId.from(ACCOUNT_ID),
                "test@example.com",
                "testuser"
        );

        // When
        userCreatedEventHandler.handle(event, "test-correlation-id");

        // Then
        Optional<AccountProfile> profileOpt = profileRepository.findByAccountId(ACCOUNT_ID);
        assertThat(profileOpt).isPresent();
        
        AccountProfile profile = profileOpt.get();
        assertThat(profile.getAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(profile.getDisplayName()).isEqualTo("testuser");
        assertThat(profile.getVersion()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should handle duplicate UserCreatedEvent idempotently")
    void shouldHandleDuplicateUserCreatedEventIdempotently() {
        // Given
        UserCreatedEvent event = new UserCreatedEvent(
                UserId.from(ACCOUNT_ID),
                "test@example.com",
                "testuser"
        );

        // When - handle event twice
        userCreatedEventHandler.handle(event, "correlation-1");
        userCreatedEventHandler.handle(event, "correlation-2");

        // Then - should only create one profile
        Optional<AccountProfile> profileOpt = profileRepository.findByAccountId(ACCOUNT_ID);
        assertThat(profileOpt).isPresent();
        assertThat(profileOpt.get().getVersion()).isEqualTo(1);
    }

    @Test
    @DisplayName("Should update profile end-to-end")
    void shouldUpdateProfileEndToEnd() {
        // Given - create profile first
        AccountProfile profile = AccountProfile.builder()
                .accountId(ACCOUNT_ID)
                .displayName("Original Name")
                .locale("en-GB")
                .timezone("Europe/London")
                .privacySettings(PrivacySettings.builder().build())
                .notificationSettings(NotificationSettings.builder().build())
                .accessibilityPreferences(AccessibilityPreferences.builder().build())
                .version(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .updatedBy(USER_ID)
                .etag("1-1704067200000")
                .build();

        AccountProfile saved = profileRepository.save(profile);
        String etag = saved.getEtag();

        UpdateProfileCommand command = UpdateProfileCommand.builder()
                .accountId(ACCOUNT_ID)
                .displayName("Updated Name")
                .locale("en-US")
                .timezone("America/New_York")
                .etag(etag)
                .build();

        // When
        AccountProfile updated = profileUseCase.updateProfile(command, USER_ID);

        // Then
        assertThat(updated).isNotNull();
        assertThat(updated.getDisplayName()).isEqualTo("Updated Name");
        assertThat(updated.getLocale()).isEqualTo("en-US");
        assertThat(updated.getTimezone()).isEqualTo("America/New_York");
        assertThat(updated.getVersion()).isEqualTo(2);
        assertThat(updated.getEtag()).isNotEqualTo(etag);
    }

    @Test
    @DisplayName("Should update privacy settings end-to-end")
    void shouldUpdatePrivacySettingsEndToEnd() {
        // Given - create profile first
        AccountProfile profile = createTestProfile();
        profileRepository.save(profile);

        UpdatePrivacySettingsCommand command = UpdatePrivacySettingsCommand.builder()
                .accountId(ACCOUNT_ID)
                .subscriptionsPrivate(true)
                .restrictedModeEnabled(true)
                .watchHistoryPrivate(true)
                .build();

        // When
        PrivacySettings updated = profileUseCase.updatePrivacySettings(command, USER_ID);

        // Then
        assertThat(updated).isNotNull();
        assertThat(updated.getSubscriptionsPrivate()).isTrue();
        assertThat(updated.getRestrictedModeEnabled()).isTrue();
        assertThat(updated.getWatchHistoryPrivate()).isTrue();

        // Verify profile was updated
        Optional<AccountProfile> updatedProfileOpt = profileRepository.findByAccountId(ACCOUNT_ID);
        assertThat(updatedProfileOpt).isPresent();
        assertThat(updatedProfileOpt.get().getVersion()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should update notification settings end-to-end")
    void shouldUpdateNotificationSettingsEndToEnd() {
        // Given - create profile first
        AccountProfile profile = createTestProfile();
        profileRepository.save(profile);

        UpdateNotificationSettingsCommand command = UpdateNotificationSettingsCommand.builder()
                .accountId(ACCOUNT_ID)
                .emailOptIn(false)
                .pushOptIn(true)
                .marketingOptIn(true)
                .build();

        // When
        NotificationSettings updated = profileUseCase.updateNotificationSettings(command, USER_ID);

        // Then
        assertThat(updated).isNotNull();
        assertThat(updated.getEmailOptIn()).isFalse();
        assertThat(updated.getPushOptIn()).isTrue();
        assertThat(updated.getMarketingOptIn()).isTrue();
    }

    @Test
    @DisplayName("Should update accessibility preferences end-to-end")
    void shouldUpdateAccessibilityPreferencesEndToEnd() {
        // Given - create profile first
        AccountProfile profile = createTestProfile();
        profileRepository.save(profile);

        UpdateAccessibilityPreferencesCommand command = UpdateAccessibilityPreferencesCommand.builder()
                .accountId(ACCOUNT_ID)
                .captionsAlwaysOn(true)
                .captionsLanguage("en")
                .autoplayDefault(false)
                .build();

        // When
        AccessibilityPreferences updated = profileUseCase.updateAccessibilityPreferences(command, USER_ID);

        // Then
        assertThat(updated).isNotNull();
        assertThat(updated.getCaptionsAlwaysOn()).isTrue();
        assertThat(updated.getCaptionsLanguage()).isEqualTo("en");
        assertThat(updated.getAutoplayDefault()).isFalse();
    }

    @Test
    @DisplayName("Should get profile end-to-end")
    void shouldGetProfileEndToEnd() {
        // Given - create profile first
        AccountProfile profile = createTestProfile();
        profileRepository.save(profile);

        GetProfileQuery query = GetProfileQuery.builder()
                .accountId(ACCOUNT_ID)
                .build();

        // When
        AccountProfile result = profileUseCase.getProfile(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(result.getDisplayName()).isEqualTo("Test User");
    }

    @Test
    @DisplayName("Should enforce optimistic locking with ETag")
    void shouldEnforceOptimisticLockingWithETag() {
        // Given - create profile
        AccountProfile profile = createTestProfile();
        AccountProfile saved = profileRepository.save(profile);
        String correctEtag = saved.getEtag();

        UpdateProfileCommand command1 = UpdateProfileCommand.builder()
                .accountId(ACCOUNT_ID)
                .displayName("First Update")
                .etag(correctEtag)
                .build();

        // When - first update succeeds
        AccountProfile updated1 = profileUseCase.updateProfile(command1, USER_ID);

        // Then - second update with old ETag should fail
        UpdateProfileCommand command2 = UpdateProfileCommand.builder()
                .accountId(ACCOUNT_ID)
                .displayName("Second Update")
                .etag(correctEtag) // Old ETag
                .build();

        assertThatThrownBy(() -> profileUseCase.updateProfile(command2, USER_ID))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("ETag mismatch");
    }

    private AccountProfile createTestProfile() {
        return AccountProfile.builder()
                .accountId(ACCOUNT_ID)
                .displayName("Test User")
                .locale("en-US")
                .country("US")
                .timezone("America/New_York")
                .privacySettings(PrivacySettings.builder().build())
                .notificationSettings(NotificationSettings.builder().build())
                .accessibilityPreferences(AccessibilityPreferences.builder().build())
                .version(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .updatedBy(USER_ID)
                .etag("1-1704067200000")
                .build();
    }
}

