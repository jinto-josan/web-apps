# Low-Level Design: Video Catalog Service

## Overview

The Video Catalog Service is a microservice responsible for managing video metadata, state transitions, and publishing workflows in a YouTube-scale platform. It implements Hexagonal Architecture with DDD-lite patterns and CQRS.

## Architecture Layers

### 1. Domain Layer (`com.youtube.mvp.videocatalog.domain`)

**Core Entity: Video**
```java
public class Video {
    private final String videoId;
    private String title;
    private String description;
    private String channelId;
    private String ownerId;
    private VideoState state;
    private VideoVisibility visibility;
    private List<LocalizedText> titles;
    private List<LocalizedText> descriptions;
    private List<String> tags;
    private String category;
    private String language;
    private String thumbnailUrl;
    private String contentUrl;
    private Duration duration;
    private long viewCount;
    private long likeCount;
    private long commentCount;
    private String version;  // ETag for optimistic locking
    private Instant createdAt;
    private Instant updatedAt;
    private Instant publishedAt;
}
```

**Domain Methods:**
- `publish()`: Transitions video from DRAFT to PUBLISHED state
- `updateMetadata()`: Updates title, description, tags, category
- `addLocalizedTitle()`: Adds multi-language title
- `addLocalizedDescription()`: Adds multi-language description
- `isViewableBy()`: Checks access control
- `incrementViewCount()`: Updates metrics

**Value Objects:**
- `LocalizedText`: Language + text pair
- `Duration`: Time in seconds
- `VideoState`: DRAFT, PUBLISHING, PUBLISHED, FAILED, DELETED
- `VideoVisibility`: PUBLIC, UNLISTED, PRIVATE

**Repository Interface:**
```java
public interface VideoRepository {
    Video save(Video video);
    Optional<Video> findById(String videoId);
    List<Video> findByChannelId(String channelId, int page, int size);
    List<Video> findByState(VideoState state, int page, int size);
    List<Video> findByVisibility(VideoVisibility visibility, int page, int size);
    boolean existsById(String videoId);
    void deleteById(String videoId);
    void updateVersion(String videoId, String version);
}
```

**Domain Service:**
```java
public interface VideoDomainService {
    String generateVersion();
    void validatePublish(Video video);
}
```

### 2. Application Layer (`com.youtube.mvp.videocatalog.application`)

**DTOs:**
- `CreateVideoRequest`: Input for creating videos
- `UpdateVideoRequest`: Input for updating videos
- `VideoResponse`: Output representation
- `LocalizedTextDto`: Localized content DTO
- `DurationDto`: Duration representation
- `PagedResponse<T>`: Generic paginated response

**Services:**

**Command Service (Write):**
```java
@Service
public class VideoCommandService {
    public VideoResponse createVideo(CreateVideoRequest request);
    public VideoResponse updateVideo(String videoId, UpdateVideoRequest request, String ifMatch);
    public VideoResponse publishVideo(String videoId);
    public void deleteVideo(String videoId);
}
```

**Query Service (Read):**
```java
@Service
public class VideoQueryService {
    public VideoResponse getVideo(String videoId);
    public PagedResponse<VideoResponse> getVideosByChannel(String channelId, int page, int size);
    public PagedResponse<VideoResponse> getVideosByState(String state, int page, int size);
    public PagedResponse<VideoResponse> getVideosByVisibility(String visibility, int page, int size);
}
```

**Mappers (MapStruct):**
```java
@Mapper(componentModel = "spring")
public interface VideoMapper {
    Video toDomain(CreateVideoRequest request);
    VideoResponse toResponse(Video video);
    LocalizedText toDomain(LocalizedTextDto dto);
    LocalizedTextDto toDto(LocalizedText localized);
}
```

### 3. Infrastructure Layer (`com.youtube.mvp.videocatalog.infrastructure`)

**Persistence Adapter (Cosmos DB):**
```java
@Component
public class VideoRepositoryAdapter implements VideoRepository {
    private final VideoCosmosRepository cosmosRepository;
    private final VideoCosmosMapper mapper;
    
    @Override
    public Video save(Video video) {
        VideoCosmosEntity entity = mapper.toEntity(video);
        VideoCosmosEntity saved = cosmosRepository.save(entity);
        return mapper.toDomain(saved);
    }
    
    // ... other methods
}
```

**Cosmos Entity:**
```java
@Container(containerName = "videos")
public class VideoCosmosEntity {
    @Id
    private String videoId;
    
    @PartitionKey
    private String partitionKey; // channelId
    
    private String title;
    private String description;
    // ... other fields
}
```

**Messaging (Outbox Pattern):**
```java
@Component
public class VideoEventPublisher {
    public void publishVideoPublishedEvent(VideoPublishedEvent event) {
        String payload = objectMapper.writeValueAsString(event);
        OutboxEvent outboxEvent = OutboxEvent.builder()
                .eventId(UUID.randomUUID().toString())
                .aggregateType("Video")
                .aggregateId(event.getVideoId())
                .eventType("VideoPublished")
                .payload(payload)
                .occurredAt(Instant.now())
                .status("PENDING")
                .build();
        outboxRepository.save(outboxEvent);
    }
}
```

**Outbox Processor:**
```java
@Component
public class OutboxProcessor {
    @Scheduled(fixedRate = 5000)
    public void processPendingEvents() {
        List<OutboxEvent> pending = outboxRepository.findAll()
                .stream()
                .filter(e -> "PENDING".equals(e.getStatus()))
                .limit(100)
                .toList();
        
        for (OutboxEvent event : pending) {
            publishToServiceBus(event);
            event.setStatus("PROCESSED");
            outboxRepository.save(event);
        }
    }
}
```

### 4. Presentation Layer (`com.youtube.mvp.videocatalog.presentation`)

**REST Controller:**
```java
@RestController
@RequestMapping("/api/v1/videos")
public class VideoController {
    
    @PostMapping
    public ResponseEntity<VideoResponse> createVideo(@Valid @RequestBody CreateVideoRequest request) {
        // ...
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<VideoResponse> getVideo(
            @PathVariable String id,
            @RequestHeader(value = "If-None-Match", required = false) String ifNoneMatch) {
        // ETag validation
        // ...
    }
    
    @PatchMapping("/{id}")
    public ResponseEntity<VideoResponse> updateVideo(
            @PathVariable String id,
            @Valid @RequestBody UpdateVideoRequest request,
            @RequestHeader(value = "If-Match", required = false) String ifMatch) {
        // Conditional update
        // ...
    }
    
    @PostMapping("/{id}/publish")
    public ResponseEntity<VideoResponse> publishVideo(@PathVariable String id) {
        // State transition + event
        // ...
    }
}
```

## Design Patterns

### 1. Hexagonal Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                    Domain (Core)                            │
│  - Video aggregate                                          │
│  - Value objects (LocalizedText, Duration)                  │
│  - Repository interface                                      │
│  - Domain services                                           │
│  - Domain events                                             │
└─────────────────────────────────────────────────────────────┘
                              ↑
                              │
┌─────────────────────────────┴───────────────────────────────┐
│              Application (Use Cases)                         │
│  - Command Service (VideoCommandService)                     │
│  - Query Service (VideoQueryService)                         │
│  - DTOs                                                      │
│  - Mappers (MapStruct)                                       │
└─────────────────────────────────────────────────────────────┘
          ↑                                    ↑
          │                                    │
┌─────────┴──────────┐            ┌───────────┴──────────────┐
│  Presentation      │            │   Infrastructure         │
│  - REST Controllers│            │   - Cosmos Adapter       │
│  - API Versioning │            │   - Service Bus          │
│  - ETag           │            │   - Outbox Processor     │
└───────────────────┘            └──────────────────────────┘
```

### 2. CQRS (Command Query Responsibility Segregation)

**Command Side (Write):**
- `VideoCommandService`: Handles all write operations
- Validates business rules
- Generates domain events
- Publishes events via Outbox

**Query Side (Read):**
- `VideoQueryService`: Handles all read operations
- Optimized read models
- No business logic
- Fast queries

### 3. Outbox Pattern

**Problem**: Ensuring reliable event publishing in distributed systems.

**Solution**: Transactional outbox
1. Save aggregate + event to database in same transaction
2. Background processor reads PENDING events
3. Publish to Service Bus
4. Mark as PROCESSED
5. Retry logic for failures

```
┌─────────────┐
│  Transaction│
│             │
│  1. Save    │─────┐
│  Video      │     │
│             │     │
│  2. Save    │─────┼───► Outbox Table (PENDING)
│  Event      │     │
│             │     │
│  COMMIT     │     │
└─────────────┘     │
                    │
                    ▼
            ┌───────────────┐
            │Outbox Table   │
            │               │
            │ id: evt-123   │
            │ status:       │
            │   PENDING     │
            └───────┬───────┘
                    │
                    │ (Scheduled Job)
                    ▼
            ┌───────────────┐
            │Outbox Processor│
            │               │
            │ 1. Read PENDING│
            │ 2. Publish S Bus│
            │ 3. Mark PROCESSED│
            └───────┬───────┘
                    │
                    ▼
            ┌───────────────┐
            │ Service Bus   │
            │               │
            │ Topic:        │
            │ video-        │
            │ published     │
            └───────────────┘
```

### 4. ETag for Optimistic Locking

**Problem**: Prevent lost updates in concurrent scenarios.

**Solution**: ETag/Version field

```
Client: GET /api/v1/videos/123
Response: ETag: "v456"

Client: PATCH /api/v1/videos/123
        If-Match: "v456"
        
Server: Validates version matches
        Updates video
        Returns new ETag: "v457"
```

### 5. Idempotency

**Problem**: Ensure safe retries of operations.

**Solution**: Idempotency-Key header

```
Client: POST /api/v1/videos
        Idempotency-Key: req-abc-123
        
Server: Check Redis cache for key
        If exists: return cached response
        Else: process + cache response
```

## Database Schema (Cosmos DB)

**Container: `videos`**
- Partition Key: `channelId`
- Indexes: `videoId`, `title`, `category`, `tags[]`, `state`, `visibility`

**Fields:**
```json
{
  "videoId": "video-123",
  "partitionKey": "channel-456",
  "title": "My Video",
  "description": "Description",
  "channelId": "channel-456",
  "ownerId": "owner-789",
  "state": "PUBLISHED",
  "visibility": "PUBLIC",
  "titles": [
    {"language": "en", "text": "My Video"},
    {"language": "es", "text": "Mi Video"}
  ],
  "tags": ["tech", "tutorial"],
  "category": "Technology",
  "language": "en",
  "durationSeconds": 3600,
  "viewCount": 1000,
  "likeCount": 50,
  "commentCount": 10,
  "version": "MTcwODAzNDAwMDAw",
  "createdAt": "2024-01-01T00:00:00Z",
  "updatedAt": "2024-01-02T00:00:00Z",
  "publishedAt": "2024-01-02T00:00:00Z"
}
```

**Container: `outbox`**
- Partition Key: `partitionKey` (date-based)
- No indexing (rarely queried, mostly inserts)

**Fields:**
```json
{
  "eventId": "evt-abc-123",
  "partitionKey": "2024-01-02",
  "aggregateType": "Video",
  "aggregateId": "video-123",
  "eventType": "VideoPublished",
  "payload": "{...}",
  "occurredAt": "2024-01-02T00:00:00Z",
  "status": "PROCESSED",
  "retryCount": 0
}
```

## Resilience Patterns

### 1. Retry
```yaml
resilience4j:
  retry:
    instances:
      video-service:
        maxAttempts: 3
        waitDuration: 1s
        exponentialBackoffMultiplier: 2
```

### 2. Circuit Breaker
```yaml
circuitbreaker:
  instances:
    video-service:
      slidingWindowSize: 100
      failureRateThreshold: 50
      waitDurationInOpenState: 10s
```

### 3. Bulkhead
```yaml
bulkhead:
  instances:
    video-service:
      maxConcurrentCalls: 10
      maxWaitDuration: 5s
```

### 4. Rate Limiter
```yaml
ratelimiter:
  instances:
    video-service:
      limitForPeriod: 100
      limitRefreshPeriod: 1s
      timeoutDuration: 5s
```

## Security

**Authentication**: OIDC with Entra External ID (Azure AD B2C)
- JWT tokens validated on every request
- Claims: `sub`, `aud`, `exp`, `iat`

**Authorization**: Check owner ownership
```java
if (!video.getOwnerId().equals(currentUserId)) {
    throw new AccessDeniedException();
}
```

## Observability

**Metrics:**
- `video.create.count`
- `video.update.count`
- `video.publish.count`
- `video.view.increment`
- HTTP latency (p50, p95, p99)

**Traces:**
- OpenTelemetry auto-instrumentation
- Custom spans for domain operations
- Correlation IDs in MDC

**Logs:**
- Structured JSON logging
- Log levels: DEBUG (local), INFO (prod)

## Error Handling

**Problem Details (RFC 7807):**
```json
{
  "type": "https://example.com/problems/video-not-found",
  "title": "Video Not Found",
  "status": 404,
  "detail": "Video with ID video-123 not found",
  "instance": "/api/v1/videos/video-123"
}
```

## Performance Considerations

1. **Cosmos DB Partitioning**: By `channelId` for optimal distribution
2. **Async Event Publishing**: Outbox pattern prevents blocking
3. **ETag Caching**: Reduces database reads
4. **Connection Pooling**: Reuse Cosmos client connections
5. **Batch Operations**: Cosmos batch writes for multi-document operations

## Scalability

1. **Horizontal Scaling**: Stateless service, multiple instances
2. **Cosmos DB Autoscale**: Automatic RU scaling
3. **Service Bus**: Partitioned topics for parallel processing
4. **HPA**: Auto-scaling based on CPU/memory (3-10 replicas)

## References

- [Hexagonal Architecture](https://alistair.cockburn.us/hexagonal-architecture/)
- [Domain-Driven Design](https://www.domainlanguage.com/ddd/)
- [CQRS Pattern](https://martinfowler.com/bliki/CQRS.html)
- [Outbox Pattern](https://microservices.io/patterns/data/transactional-outbox.html)
- [RFC 7807 - Problem Details](https://tools.ietf.org/html/rfc7807)

