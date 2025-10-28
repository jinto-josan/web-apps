package com.youtube.mvp.videocatalog.integration;

import com.azure.spring.data.cosmos.CosmosClientBuilder;
import com.azure.spring.data.cosmos.core.CosmosTemplate;
import com.youtube.mvp.videocatalog.domain.model.Video;
import com.youtube.mvp.videocatalog.domain.model.VideoState;
import com.youtube.mvp.videocatalog.domain.model.VideoVisibility;
import com.youtube.mvp.videocatalog.domain.repository.VideoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.testcontainers.containers.CosmosEmulatorContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
@ActiveProfiles("test")
class VideoCatalogIntegrationTest {
    
    @Container
    static CosmosEmulatorContainer cosmosEmulator = new CosmosEmulatorContainer(
            DockerImageName.parse("mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator:latest")
    );
    
    @Autowired
    private VideoRepository videoRepository;
    
    @BeforeEach
    void setUp() {
        // Cleanup before each test
        // In real scenario, you'd use a test database or testcontainers properly configured
    }
    
    @Test
    void shouldCreateAndRetrieveVideo() {
        // Given
        Video video = Video.builder()
                .videoId("video-123")
                .title("Test Video")
                .description("Test Description")
                .channelId("channel-123")
                .ownerId("owner-123")
                .state(VideoState.DRAFT)
                .visibility(VideoVisibility.PUBLIC)
                .version("v1")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        // When
        Video saved = videoRepository.save(video);
        
        // Then
        assertThat(saved).isNotNull();
        assertThat(saved.getVideoId()).isEqualTo("video-123");
        
        Optional<Video> retrieved = videoRepository.findById("video-123");
        assertThat(retrieved).isPresent();
        assertThat(retrieved.get().getTitle()).isEqualTo("Test Video");
    }
    
    @Test
    void shouldUpdateVideo() {
        // Given
        Video video = Video.builder()
                .videoId("video-456")
                .title("Original Title")
                .description("Original Description")
                .channelId("channel-123")
                .ownerId("owner-123")
                .state(VideoState.DRAFT)
                .visibility(VideoVisibility.PUBLIC)
                .version("v1")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();
        
        videoRepository.save(video);
        
        // When
        Video updated = video.toBuilder()
                .title("Updated Title")
                .description("Updated Description")
                .version("v2")
                .updatedAt(Instant.now())
                .build();
        
        Video saved = videoRepository.save(updated);
        
        // Then
        assertThat(saved.getTitle()).isEqualTo("Updated Title");
        assertThat(saved.getVersion()).isEqualTo("v2");
    }
}

