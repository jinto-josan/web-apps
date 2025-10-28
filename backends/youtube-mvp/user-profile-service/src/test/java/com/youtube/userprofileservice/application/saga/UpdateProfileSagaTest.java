package com.youtube.userprofileservice.application.saga;

import com.youtube.userprofileservice.application.commands.UpdateProfileCommand;
import com.youtube.userprofileservice.domain.entities.AccountProfile;
import com.youtube.userprofileservice.domain.entities.AccessibilityPreferences;
import com.youtube.userprofileservice.domain.entities.NotificationSettings;
import com.youtube.userprofileservice.domain.entities.PrivacySettings;
import com.youtube.userprofileservice.domain.repositories.ProfileRepository;
import com.youtube.userprofileservice.domain.services.BlobUriValidator;
import com.youtube.userprofileservice.domain.services.CacheService;
import com.youtube.userprofileservice.domain.services.EventPublisher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UpdateProfileSaga.
 */
@ExtendWith(MockitoExtension.class)
class UpdateProfileSagaTest {
    
    @Mock
    private ProfileRepository profileRepository;
    
    @Mock
    private EventPublisher eventPublisher;
    
    @Mock
    private CacheService cacheService;
    
    @Mock
    private BlobUriValidator blobUriValidator;
    
    private UpdateProfileSaga saga;
    private String sagaId = "01JXX0X5X5X5X5X5X5X5X5X5X";
    private String accountId = "01JXX0X5X5X5X5X5X5X5X5X5Y";
    private String updatedBy = "01JXX0X5X5X5X5X5X5X5X5X5Z";
    
    @BeforeEach
    void setUp() {
        UpdateProfileCommand command = UpdateProfileCommand.builder()
                .accountId(accountId)
                .displayName("Updated Name")
                .locale("en-US")
                .timezone("America/New_York")
                .build();
        
        saga = new UpdateProfileSaga(
                sagaId,
                command,
                updatedBy,
                profileRepository,
                eventPublisher,
                cacheService,
                blobUriValidator
        );
    }
    
    @Test
    void shouldExecuteSagaSuccessfully() throws SagaExecutionException {
        // Given
        AccountProfile existingProfile = AccountProfile.builder()
                .accountId(accountId)
                .displayName("Original Name")
                .photoUrl(null)
                .locale("en-GB")
                .country("GB")
                .timezone("Europe/London")
                .privacySettings(PrivacySettings.builder().build())
                .notificationSettings(NotificationSettings.builder().build())
                .accessibilityPreferences(AccessibilityPreferences.builder().build())
                .version(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .updatedBy("original-user")
                .etag("original-etag")
                .build();
        
        when(profileRepository.findByAccountId(accountId))
                .thenReturn(Optional.of(existingProfile));
        
        when(profileRepository.update(any(AccountProfile.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        
        // When
        AccountProfile result = saga.execute();
        
        // Then
        assertThat(result).isNotNull();
        assertThat(result.getDisplayName()).isEqualTo("Updated Name");
        assertThat(result.getLocale()).isEqualTo("en-US");
        assertThat(result.getTimezone()).isEqualTo("America/New_York");
        assertThat(result.getVersion()).isEqualTo(2);
        
        verify(profileRepository).findByAccountId(accountId);
        verify(profileRepository).update(any(AccountProfile.class));
        verify(eventPublisher).publishProfileUpdated(any());
        verify(cacheService).invalidateProfile(accountId);
    }
    
    @Test
    void shouldThrowExceptionWhenProfileNotFound() {
        // Given
        when(profileRepository.findByAccountId(accountId))
                .thenReturn(Optional.empty());
        
        // When & Then
        assertThatThrownBy(() -> saga.execute())
                .isInstanceOf(SagaExecutionException.class);
        
        verify(profileRepository).findByAccountId(accountId);
        verify(profileRepository, never()).update(any());
    }
    
    @Test
    void shouldThrowExceptionWhenETagMismatch() {
        // Given
        UpdateProfileCommand commandWithEtag = UpdateProfileCommand.builder()
                .accountId(accountId)
                .displayName("Updated Name")
                .etag("wrong-etag")
                .build();
        
        saga = new UpdateProfileSaga(
                sagaId,
                commandWithEtag,
                updatedBy,
                profileRepository,
                eventPublisher,
                cacheService,
                blobUriValidator
        );
        
        AccountProfile existingProfile = AccountProfile.builder()
                .accountId(accountId)
                .displayName("Original Name")
                .version(1)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .etag("correct-etag")
                .privacySettings(PrivacySettings.builder().build())
                .notificationSettings(NotificationSettings.builder().build())
                .accessibilityPreferences(AccessibilityPreferences.builder().build())
                .build();
        
        when(profileRepository.findByAccountId(accountId))
                .thenReturn(Optional.of(existingProfile));
        
        // When & Then
        assertThatThrownBy(() -> saga.execute())
                .isInstanceOf(SagaExecutionException.class);
        
        verify(profileRepository).findByAccountId(accountId);
        verify(profileRepository, never()).update(any());
    }
}

