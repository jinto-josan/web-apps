package com.youtube.userprofileservice.interfaces.rest;

import com.youtube.common.domain.services.correlation.CorrelationContext;
import com.youtube.userprofileservice.application.commands.*;
import com.youtube.userprofileservice.application.queries.*;
import com.youtube.userprofileservice.application.usecases.ProfileUseCase;
import com.youtube.userprofileservice.domain.entities.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProfileController.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProfileController Tests")
class ProfileControllerTest {

    @Mock
    private ProfileUseCase profileUseCase;

    @Mock
    private Jwt jwt;

    @InjectMocks
    private ProfileController controller;

    private static final String ACCOUNT_ID = "01JXX0X5X5X5X5X5X5X5X5X5X";
    private static final String USER_ID = "01JXX0X5X5X5X5X5X5X5X5X5Y";
    private static final String CORRELATION_ID = "test-correlation-id";

    @BeforeEach
    void setUp() {
        when(jwt.getClaimAsString("sub")).thenReturn(USER_ID);
        CorrelationContext.setCorrelationId(CORRELATION_ID);
    }

    @Test
    @DisplayName("Should get profile successfully")
    void shouldGetProfileSuccessfully() {
        // Given
        AccountProfile profile = createTestProfile();
        when(profileUseCase.getProfile(any(GetProfileQuery.class))).thenReturn(profile);

        // When
        ResponseEntity<AccountProfile> response = controller.getProfile(ACCOUNT_ID, jwt);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getAccountId()).isEqualTo(ACCOUNT_ID);
        assertThat(response.getHeaders().getETag()).isNotNull();
        verify(profileUseCase).getProfile(any(GetProfileQuery.class));
    }

    @Test
    @DisplayName("Should update profile successfully")
    void shouldUpdateProfileSuccessfully() {
        // Given
        UpdateProfileCommand command = UpdateProfileCommand.builder()
                .displayName("New Name")
                .build();
        AccountProfile updatedProfile = createTestProfile();
        updatedProfile.setDisplayName("New Name");

        when(profileUseCase.updateProfile(any(UpdateProfileCommand.class), eq(USER_ID)))
                .thenReturn(updatedProfile);

        // When
        ResponseEntity<AccountProfile> response = controller.updateProfile(
                ACCOUNT_ID, command, null, jwt);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDisplayName()).isEqualTo("New Name");
        verify(profileUseCase).updateProfile(any(UpdateProfileCommand.class), eq(USER_ID));
    }

    @Test
    @DisplayName("Should get privacy settings successfully")
    void shouldGetPrivacySettingsSuccessfully() {
        // Given
        AccountProfile profile = createTestProfile();
        when(profileUseCase.getProfile(any(GetProfileQuery.class))).thenReturn(profile);
        when(profileUseCase.getPrivacySettings(any(GetPrivacySettingsQuery.class)))
                .thenReturn(profile.getPrivacySettings());

        // When
        ResponseEntity<PrivacySettings> response = controller.getPrivacySettings(ACCOUNT_ID, jwt);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(profileUseCase).getPrivacySettings(any(GetPrivacySettingsQuery.class));
    }

    @Test
    @DisplayName("Should update privacy settings successfully")
    void shouldUpdatePrivacySettingsSuccessfully() {
        // Given
        UpdatePrivacySettingsCommand command = UpdatePrivacySettingsCommand.builder()
                .subscriptionsPrivate(true)
                .build();
        PrivacySettings settings = PrivacySettings.builder()
                .subscriptionsPrivate(true)
                .build();

        when(profileUseCase.updatePrivacySettings(any(UpdatePrivacySettingsCommand.class), eq(USER_ID)))
                .thenReturn(settings);

        // When
        ResponseEntity<PrivacySettings> response = controller.updatePrivacySettings(
                ACCOUNT_ID, command, jwt);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getSubscriptionsPrivate()).isTrue();
        verify(profileUseCase).updatePrivacySettings(any(UpdatePrivacySettingsCommand.class), eq(USER_ID));
    }

    @Test
    @DisplayName("Should get notification settings successfully")
    void shouldGetNotificationSettingsSuccessfully() {
        // Given
        NotificationSettings settings = NotificationSettings.builder().build();
        when(profileUseCase.getNotificationSettings(any(GetNotificationSettingsQuery.class)))
                .thenReturn(settings);

        // When
        ResponseEntity<NotificationSettings> response = controller.getNotificationSettings(ACCOUNT_ID, jwt);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        verify(profileUseCase).getNotificationSettings(any(GetNotificationSettingsQuery.class));
    }

    @Test
    @DisplayName("Should update notification settings successfully")
    void shouldUpdateNotificationSettingsSuccessfully() {
        // Given
        UpdateNotificationSettingsCommand command = UpdateNotificationSettingsCommand.builder()
                .emailOptIn(false)
                .build();
        NotificationSettings settings = NotificationSettings.builder()
                .emailOptIn(false)
                .build();

        when(profileUseCase.updateNotificationSettings(any(UpdateNotificationSettingsCommand.class), eq(USER_ID)))
                .thenReturn(settings);

        // When
        ResponseEntity<NotificationSettings> response = controller.updateNotificationSettings(
                ACCOUNT_ID, command, jwt);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getEmailOptIn()).isFalse();
        verify(profileUseCase).updateNotificationSettings(any(UpdateNotificationSettingsCommand.class), eq(USER_ID));
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

