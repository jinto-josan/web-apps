package com.youtube.userprofileservice.infrastructure.messaging;

import com.youtube.common.domain.core.Clock;
import com.youtube.common.domain.core.DomainEvent;
import com.youtube.common.domain.core.UnitOfWork;
import com.youtube.common.domain.events.UserCreatedEvent;
import com.youtube.common.domain.shared.valueobjects.UserId;
import com.youtube.userprofileservice.domain.entities.AccountProfile;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserCreatedEventHandler.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("UserCreatedEventHandler Tests")
class UserCreatedEventHandlerTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private Clock clock;

    @Mock
    private UnitOfWork unitOfWork;

    @InjectMocks
    private UserCreatedEventHandler handler;

    private static final String USER_ID = "01JXX0X5X5X5X5X5X5X5X5X5X";
    private static final String EMAIL = "test@example.com";
    private static final String USERNAME = "testuser";
    private static final Instant NOW = Instant.parse("2024-01-01T00:00:00Z");
    private static final String CORRELATION_ID = "test-correlation-id";

    @BeforeEach
    void setUp() {
        when(clock.now()).thenReturn(NOW);
        doNothing().when(unitOfWork).begin();
        doNothing().when(unitOfWork).rollback(any(Exception.class));
    }

    @Nested
    @DisplayName("Handle UserCreatedEvent Tests")
    class HandleUserCreatedEventTests {

        @Test
        @DisplayName("Should create profile when user is created")
        void shouldCreateProfileWhenUserIsCreated() {
            // Given
            UserCreatedEvent event = new UserCreatedEvent(
                    UserId.from(USER_ID),
                    EMAIL,
                    USERNAME
            );

            when(profileRepository.exists(USER_ID)).thenReturn(false);
            when(profileRepository.save(any(AccountProfile.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));

            // When
            handler.handle(event, CORRELATION_ID);

            // Then
            verify(profileRepository).exists(USER_ID);
            verify(profileRepository).save(any(AccountProfile.class));
            verify(unitOfWork).begin();
        }

        @Test
        @DisplayName("Should not create profile if already exists (idempotency)")
        void shouldNotCreateProfileIfAlreadyExists() {
            // Given
            UserCreatedEvent event = new UserCreatedEvent(
                    UserId.from(USER_ID),
                    EMAIL,
                    USERNAME
            );

            when(profileRepository.exists(USER_ID)).thenReturn(true);

            // When
            handler.handle(event, CORRELATION_ID);

            // Then
            verify(profileRepository).exists(USER_ID);
            verify(profileRepository, never()).save(any(AccountProfile.class));
        }

        @Test
        @DisplayName("Should use email prefix as display name when username is null")
        void shouldUseEmailPrefixAsDisplayNameWhenUsernameIsNull() {
            // Given
            UserCreatedEvent event = new UserCreatedEvent(
                    UserId.from(USER_ID),
                    EMAIL,
                    null
            );

            when(profileRepository.exists(USER_ID)).thenReturn(false);
            when(profileRepository.save(any(AccountProfile.class)))
                    .thenAnswer(invocation -> {
                        AccountProfile profile = invocation.getArgument(0);
                        assertThat(profile.getDisplayName()).isEqualTo("test");
                        return profile;
                    });

            // When
            handler.handle(event, CORRELATION_ID);

            // Then
            verify(profileRepository).save(any(AccountProfile.class));
        }

        @Test
        @DisplayName("Should use default display name when both username and email are null")
        void shouldUseDefaultDisplayNameWhenBothUsernameAndEmailAreNull() {
            // Given
            UserCreatedEvent event = new UserCreatedEvent(
                    UserId.from(USER_ID),
                    null,
                    null
            );

            when(profileRepository.exists(USER_ID)).thenReturn(false);
            when(profileRepository.save(any(AccountProfile.class)))
                    .thenAnswer(invocation -> {
                        AccountProfile profile = invocation.getArgument(0);
                        assertThat(profile.getDisplayName()).isEqualTo("User");
                        return profile;
                    });

            // When
            handler.handle(event, CORRELATION_ID);

            // Then
            verify(profileRepository).save(any(AccountProfile.class));
        }

        @Test
        @DisplayName("Should rollback transaction on error")
        void shouldRollbackTransactionOnError() {
            // Given
            UserCreatedEvent event = new UserCreatedEvent(
                    UserId.from(USER_ID),
                    EMAIL,
                    USERNAME
            );

            when(profileRepository.exists(USER_ID)).thenReturn(false);
            when(profileRepository.save(any(AccountProfile.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // When & Then
            assertThatThrownBy(() -> handler.handle(event, CORRELATION_ID))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Database error");

            verify(unitOfWork).rollback(any(Exception.class));
        }

        @Test
        @DisplayName("Should throw exception for non-UserCreatedEvent")
        void shouldThrowExceptionForNonUserCreatedEvent() {
            // Given
            DomainEvent event = new DomainEvent() {
                @Override
                public String getEventType() {
                    return "other.event";
                }
            };

            // When & Then
            assertThatThrownBy(() -> handler.handle(event, CORRELATION_ID))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Expected UserCreatedEvent");
        }
    }
}

