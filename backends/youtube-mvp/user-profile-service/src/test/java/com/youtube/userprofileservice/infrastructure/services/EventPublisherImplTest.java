package com.youtube.userprofileservice.infrastructure.services;

import com.youtube.common.domain.events.EventPublisher;
import com.youtube.userprofileservice.domain.events.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;

import static org.mockito.Mockito.verify;

/**
 * Unit tests for EventPublisherImpl.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("EventPublisherImpl Tests")
class EventPublisherImplTest {

    @Mock
    private EventPublisher commonDomainEventPublisher;

    @InjectMocks
    private EventPublisherImpl eventPublisher;

    @BeforeEach
    void setUp() {
        // Setup if needed
    }

    @Test
    @DisplayName("Should publish ProfileUpdated event")
    void shouldPublishProfileUpdatedEvent() {
        // Given
        ProfileUpdated event = new ProfileUpdated("account-123", "user-456", "displayName,locale");

        // When
        eventPublisher.publishProfileUpdated(event);

        // Then
        verify(commonDomainEventPublisher).publishAll(List.of(event));
    }

    @Test
    @DisplayName("Should publish PrivacySettingsChanged event")
    void shouldPublishPrivacySettingsChangedEvent() {
        // Given
        PrivacySettingsChanged event = new PrivacySettingsChanged("account-123", "user-456", "subscriptionsPrivate", true);

        // When
        eventPublisher.publishPrivacySettingsChanged(event);

        // Then
        verify(commonDomainEventPublisher).publishAll(List.of(event));
    }

    @Test
    @DisplayName("Should publish NotificationPrefsChanged event")
    void shouldPublishNotificationPrefsChangedEvent() {
        // Given
        NotificationPrefsChanged event = new NotificationPrefsChanged("account-123", "user-456", "emailOptIn", false);

        // When
        eventPublisher.publishNotificationPrefsChanged(event);

        // Then
        verify(commonDomainEventPublisher).publishAll(List.of(event));
    }

    @Test
    @DisplayName("Should publish AccessibilityPrefsChanged event")
    void shouldPublishAccessibilityPrefsChangedEvent() {
        // Given
        AccessibilityPrefsChanged event = new AccessibilityPrefsChanged("account-123", "user-456", "captionsAlwaysOn", "true");

        // When
        eventPublisher.publishAccessibilityPrefsChanged(event);

        // Then
        verify(commonDomainEventPublisher).publishAll(List.of(event));
    }
}

