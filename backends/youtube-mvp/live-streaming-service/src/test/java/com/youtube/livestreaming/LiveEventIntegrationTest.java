package com.youtube.livestreaming;

import com.youtube.livestreaming.domain.entities.LiveEvent;
import com.youtube.livestreaming.domain.valueobjects.LiveEventConfiguration;
import com.youtube.livestreaming.infrastructure.persistence.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Testcontainers
@ActiveProfiles("test")
class LiveEventIntegrationTest {
    
    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("livestreaming_test")
            .withUsername("test")
            .withPassword("test");
    
    @Autowired
    private JpaLiveEventRepository repository;
    
    @Test
    void shouldSaveAndRetrieveLiveEvent() {
        // Given
        var config = LiveEventConfiguration.builder()
            .name("Test Live Event")
            .description("Test description")
            .channelId("channel-123")
            .userId("user-456")
            .dvrEnabled(true)
            .dvrWindowInMinutes(120)
            .build();
        
        var liveEvent = new LiveEvent("live-1", "user-456", "channel-123", config);
        
        // When
        var saved = repository.save(liveEvent);
        var retrieved = repository.findById(saved.getId());
        
        // Then
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getId()).isEqualTo("live-1");
        assertThat(retrieved.get().getChannelId()).isEqualTo("channel-123");
        assertThat(retrieved.get().getUserId()).isEqualTo("user-456");
    }
    
    @Test
    void shouldFindLiveEventByUserIdAndId() {
        // Given
        var config = LiveEventConfiguration.builder()
            .name("Test Event")
            .channelId("channel-1")
            .userId("user-1")
            .build();
        
        var liveEvent = new LiveEvent("live-1", "user-1", "channel-1", config);
        repository.save(liveEvent);
        
        // When
        Optional<LiveEvent> found = repository.findByIdAndUserId("live-1", "user-1");
        
        // Then
        assertThat(found).isPresent();
    }
    
    @Test
    void shouldFindLiveEventsByChannelId() {
        // Given
        var config = LiveEventConfiguration.builder()
            .name("Event 1")
            .channelId("channel-1")
            .userId("user-1")
            .build();
        
        repository.save(new LiveEvent("live-1", "user-1", "channel-1", config));
        repository.save(new LiveEvent("live-2", "user-1", "channel-1", config));
        
        // When
        var events = repository.findByChannelId("channel-1");
        
        // Then
        assertThat(events).hasSize(2);
    }
}

