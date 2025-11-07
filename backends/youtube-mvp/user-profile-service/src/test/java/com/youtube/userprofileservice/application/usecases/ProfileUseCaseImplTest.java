package com.youtube.userprofileservice.application.usecases;

import com.youtube.common.domain.core.Clock;
import com.youtube.common.domain.core.UnitOfWork;
import com.youtube.common.domain.events.EventPublisher;
import com.youtube.common.domain.error.ConflictException;
import com.youtube.common.domain.services.correlation.CorrelationContext;
import com.youtube.userprofileservice.application.commands.*;
import com.youtube.userprofileservice.application.queries.*;
import com.youtube.userprofileservice.domain.entities.*;
import com.youtube.userprofileservice.domain.repositories.ProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProfileUseCaseImpl.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileUseCaseImpl Tests")
class ProfileUseCaseImplTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private UnitOfWork unitOfWork;

    @Mock
    private Clock clock;

    @InjectMocks
    private ProfileUseCaseImpl profileUseCase;

    private static final String ACCOUNT_ID = "01JXX0X5X5X5X5X5X5X5X5X5X";
    private static final String UPDATED_BY = "01JXX0X5X5X5X5X5X5X5X5X5Y";
    private static final Instant NOW = Instant.parse("2024-01-01T00:00:00Z");
    private static final String ETAG = "1-1704067200000";

    @BeforeEach
    void setUp() {
        when(clock.now()).thenReturn(NOW);
        doNothing().when(unitOfWork).begin();
        doNothing().when(unitOfWork).rollback(any(Exception.class));
        CorrelationContext.setCorrelationId("test-correlation-id");
    }

    @Nested
    @DisplayName("Update Profile Tests")
    class UpdateProfileTests {

        @Test
        @DisplayName("Should update profile successfully")
        void shouldUpdateProfileSuccessfully() {
            // Given
            AccountProfile existingProfile = createTestProfile();
            UpdateProfileCommand command = UpdateProfileCommand.builder()
                    .accountId(ACCOUNT_ID)
                    .displayName("New Display Name")
                    .locale("en-US")
                    .timezone("America/New_York")
                    .etag(ETAG)
                    .build();

            when(profileRepository.findByAccountId(ACCOUNT_ID))
                    .thenReturn(Optional.of(existingProfile));
            when(profileRepository.update(any(AccountProfile.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            AccountProfile result = profileUseCase.updateProfile(command, UPDATED_BY);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getDisplayName()).isEqualTo("New Display Name");
            assertThat(result.getLocale()).isEqualTo("en-US");
            assertThat(result.getTimezone()).isEqualTo("America/New_York");
            assertThat(result.getVersion()).isEqualTo(2);
            assertThat(result.getUpdatedBy()).isEqualTo(UPDATED_BY);

            verify(profileRepository).findByAccountId(ACCOUNT_ID);
            verify(profileRepository).update(any(AccountProfile.class));
            verify(eventPublisher).publishAll(anyList());
            verify(unitOfWork).begin();
        }

        @Test
        @DisplayName("Should throw exception when profile not found")
        void shouldThrowExceptionWhenProfileNotFound() {
            // Given
            UpdateProfileCommand command = UpdateProfileCommand.builder()
                    .accountId(ACCOUNT_ID)
                    .displayName("New Name")
                    .build();

            when(profileRepository.findByAccountId(ACCOUNT_ID))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> profileUseCase.updateProfile(command, UPDATED_BY))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Profile not found");

            verify(profileRepository).findByAccountId(ACCOUNT_ID);
            verify(profileRepository, never()).update(any());
            verify(unitOfWork).rollback(any(Exception.class));
        }

        @Test
        @DisplayName("Should throw ConflictException when ETag mismatch")
        void shouldThrowConflictExceptionWhenETagMismatch() {
            // Given
            AccountProfile existingProfile = createTestProfile();
            UpdateProfileCommand command = UpdateProfileCommand.builder()
                    .accountId(ACCOUNT_ID)
                    .displayName("New Name")
                    .etag("wrong-etag")
                    .build();

            when(profileRepository.findByAccountId(ACCOUNT_ID))
                    .thenReturn(Optional.of(existingProfile));

            // When & Then
            assertThatThrownBy(() -> profileUseCase.updateProfile(command, UPDATED_BY))
                    .isInstanceOf(ConflictException.class)
                    .hasMessageContaining("ETag mismatch");

            verify(profileRepository).findByAccountId(ACCOUNT_ID);
            verify(profileRepository, never()).update(any());
            verify(unitOfWork).rollback(any(Exception.class));
        }

        @Test
        @DisplayName("Should not publish event when no fields changed")
        void shouldNotPublishEventWhenNoFieldsChanged() {
            // Given
            AccountProfile existingProfile = createTestProfile();
            UpdateProfileCommand command = UpdateProfileCommand.builder()
                    .accountId(ACCOUNT_ID)
                    .displayName(existingProfile.getDisplayName()) // Same value
                    .etag(ETAG)
                    .build();

            when(profileRepository.findByAccountId(ACCOUNT_ID))
                    .thenReturn(Optional.of(existingProfile));
            when(profileRepository.update(any(AccountProfile.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            AccountProfile result = profileUseCase.updateProfile(command, UPDATED_BY);

            // Then
            assertThat(result).isNotNull();
            verify(eventPublisher, never()).publishAll(anyList());
        }
    }

    @Nested
    @DisplayName("Update Privacy Settings Tests")
    class UpdatePrivacySettingsTests {

        @Test
        @DisplayName("Should update privacy settings successfully")
        void shouldUpdatePrivacySettingsSuccessfully() {
            // Given
            AccountProfile existingProfile = createTestProfile();
            UpdatePrivacySettingsCommand command = UpdatePrivacySettingsCommand.builder()
                    .accountId(ACCOUNT_ID)
                    .subscriptionsPrivate(true)
                    .restrictedModeEnabled(true)
                    .build();

            when(profileRepository.findByAccountId(ACCOUNT_ID))
                    .thenReturn(Optional.of(existingProfile));
            when(profileRepository.update(any(AccountProfile.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            PrivacySettings result = profileUseCase.updatePrivacySettings(command, UPDATED_BY);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getSubscriptionsPrivate()).isTrue();
            assertThat(result.getRestrictedModeEnabled()).isTrue();

            verify(profileRepository).findByAccountId(ACCOUNT_ID);
            verify(profileRepository).update(any(AccountProfile.class));
            verify(eventPublisher).publishAll(anyList());
        }
    }

    @Nested
    @DisplayName("Update Notification Settings Tests")
    class UpdateNotificationSettingsTests {

        @Test
        @DisplayName("Should update notification settings successfully")
        void shouldUpdateNotificationSettingsSuccessfully() {
            // Given
            AccountProfile existingProfile = createTestProfile();
            UpdateNotificationSettingsCommand command = UpdateNotificationSettingsCommand.builder()
                    .accountId(ACCOUNT_ID)
                    .emailOptIn(false)
                    .marketingOptIn(true)
                    .build();

            when(profileRepository.findByAccountId(ACCOUNT_ID))
                    .thenReturn(Optional.of(existingProfile));
            when(profileRepository.update(any(AccountProfile.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            NotificationSettings result = profileUseCase.updateNotificationSettings(command, UPDATED_BY);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getEmailOptIn()).isFalse();
            assertThat(result.getMarketingOptIn()).isTrue();

            verify(profileRepository).findByAccountId(ACCOUNT_ID);
            verify(profileRepository).update(any(AccountProfile.class));
            verify(eventPublisher).publishAll(anyList());
        }
    }

    @Nested
    @DisplayName("Update Accessibility Preferences Tests")
    class UpdateAccessibilityPreferencesTests {

        @Test
        @DisplayName("Should update accessibility preferences successfully")
        void shouldUpdateAccessibilityPreferencesSuccessfully() {
            // Given
            AccountProfile existingProfile = createTestProfile();
            UpdateAccessibilityPreferencesCommand command = UpdateAccessibilityPreferencesCommand.builder()
                    .accountId(ACCOUNT_ID)
                    .captionsAlwaysOn(true)
                    .captionsLanguage("en")
                    .build();

            when(profileRepository.findByAccountId(ACCOUNT_ID))
                    .thenReturn(Optional.of(existingProfile));
            when(profileRepository.update(any(AccountProfile.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            AccessibilityPreferences result = profileUseCase.updateAccessibilityPreferences(command, UPDATED_BY);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getCaptionsAlwaysOn()).isTrue();
            assertThat(result.getCaptionsLanguage()).isEqualTo("en");

            verify(profileRepository).findByAccountId(ACCOUNT_ID);
            verify(profileRepository).update(any(AccountProfile.class));
            verify(eventPublisher).publishAll(anyList());
        }
    }

    @Nested
    @DisplayName("Get Profile Tests")
    class GetProfileTests {

        @Test
        @DisplayName("Should get profile successfully")
        void shouldGetProfileSuccessfully() {
            // Given
            AccountProfile profile = createTestProfile();
            GetProfileQuery query = GetProfileQuery.builder()
                    .accountId(ACCOUNT_ID)
                    .build();

            when(profileRepository.findByAccountId(ACCOUNT_ID))
                    .thenReturn(Optional.of(profile));

            // When
            AccountProfile result = profileUseCase.getProfile(query);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getAccountId()).isEqualTo(ACCOUNT_ID);
            verify(profileRepository).findByAccountId(ACCOUNT_ID);
        }

        @Test
        @DisplayName("Should throw exception when profile not found")
        void shouldThrowExceptionWhenProfileNotFound() {
            // Given
            GetProfileQuery query = GetProfileQuery.builder()
                    .accountId(ACCOUNT_ID)
                    .build();

            when(profileRepository.findByAccountId(ACCOUNT_ID))
                    .thenReturn(Optional.empty());

            // When & Then
            assertThatThrownBy(() -> profileUseCase.getProfile(query))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Profile not found");
        }
    }

    private AccountProfile createTestProfile() {
        return AccountProfile.builder()
                .accountId(ACCOUNT_ID)
                .displayName("Test User")
                .locale("en-GB")
                .country("GB")
                .timezone("Europe/London")
                .privacySettings(PrivacySettings.builder().build())
                .notificationSettings(NotificationSettings.builder().build())
                .accessibilityPreferences(AccessibilityPreferences.builder().build())
                .version(1)
                .createdAt(NOW.minusSeconds(3600))
                .updatedAt(NOW.minusSeconds(3600))
                .updatedBy("original-user")
                .etag(ETAG)
                .build();
    }
}

