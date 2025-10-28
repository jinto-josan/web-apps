# Video Upload Service Architecture

## Overview

A production-ready video upload service designed for YouTube-scale platforms. Handles multi-Gbps ingest with pre-signed URLs, resumable chunked uploads, robust saga-based orchestration, and comprehensive resilience patterns.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────────┐
│                         Client                                    │
│                    (Browser/Mobile App)                          │
└──────────────────────┬─────────────────────────────────────────┘
                       │
                       │ 1. POST /api/v1/uploads/initialize
                       │    { title, size, contentType }
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                      REST Controller                              │
│              VideoUploadController                                │
│  - Initialize upload                                             │
│  - Check status                                                  │
│  - Cancel upload                                                 │
└──────────────────────┬─────────────────────────────────────────┘
                       │
                       │ 2. Execute Saga
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                   InitializeUploadSaga                           │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │ Step 1: Validate Request                                 │   │
│  │   - Check file size                                      │   │
│  │   - Validate content type                                │   │
│  │                                                           │   │
│  │ Step 2: Check Quota                                     │   │
│  │   - Verify remaining upload quota                        │   │
│  │   - Consume quota for this upload                       │   │
│  │   Compensation: Release quota if fails later              │   │
│  │                                                           │   │
│  │ Step 3: Generate Pre-signed URL                         │   │
│  │   - Azure Blob Storage SAS token                         │   │
│  │   - Direct upload capability                             │   │
│  │                                                           │   │
│  │ Step 4: Create Upload Session                           │   │
│  │   - Persist upload metadata                              │   │
│  │   - Track progress                                       │   │
│  │   Compensation: Delete session                            │   │
│  └─────────────────────────────────────────────────────────┘   │
└──────────────────────┬─────────────────────────────────────────┘
                       │
                       │ 3. Return Pre-signed URL
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                       Client                                     │
│                 Direct upload to Azure Blob Storage              │
│               (No server intermediation)                         │
└──────────────────────┬─────────────────────────────────────────┘
                       │
                       │ 4. Upload complete → Event Grid
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                   Azure Event Grid                               │
│              Blob created event                                 │
└──────────────────────┬─────────────────────────────────────────┘
                       │
                       │ 5. Trigger transcode
                       ▼
┌─────────────────────────────────────────────────────────────────┐
│                  Video Transcode Service                         │
```

## Design Patterns

### 1. Clean Architecture
- **Domain Layer**: Entities, Value Objects, Domain Services
- **Application Layer**: Use Cases, Saga Orchestration
- **Infrastructure Layer**: Repositories, External Services
- **Interface Layer**: REST Controllers, Event Handlers

### 2. Saga Pattern
Orchestrates distributed transactions with compensation:
- **Validation Step**: File size, content type checks
- **Quota Check Step**: Verify and consume user quota
- **URL Generation Step**: Create Azure SAS token
- **Session Creation Step**: Persist upload metadata

**Compensation Flow:**
- Release consumed quota on failure
- Delete created sessions
- Clean up temporary resources

### 3. Repository Pattern
Abstracts data access:
- `VideoUploadRepository`: Upload session management
- `UploadQuotaRepository`: Quota tracking and enforcement

### 4. Factory Pattern
Centralized object creation for saga steps and upload sessions

### 5. Strategy Pattern
Pluggable validation and quota strategies

## Azure Integration

### Azure Blob Storage
- **Hot Tier**: Fast access for source videos
- **Pre-signed URLs**: Direct client uploads (no server bandwidth)
- **Chunked Uploads**: Resumable multi-part uploads
- **SAS Tokens**: Time-limited, permission-scoped access

### Azure Service Bus
- **Saga Orchestration**: Distributed transaction coordination
- **Reliable Messaging**: At-least-once delivery guarantees
- **Dead Letter Queue**: Failed message handling

### Azure Event Grid
- **Blob Notifications**: Trigger on blob creation
- **Event-driven Processing**: Trigger transcode pipeline
- **Decoupled Architecture**: Loosely coupled services

## Resilience Patterns

### Circuit Breaker
Protects against cascading failures:
```java
@CircuitBreaker(name = "blobStorage", fallbackMethod = "fallback")
```

### Retry
Handles transient failures:
```java
@Retry(name = "blobStorage")
```

### Timeout
Prevents hanging operations:
```java
@TimeLimiter(name = "blobStorage")
```

## Performance Targets

| Metric | Target | Implementation |
|--------|--------|----------------|
| P95 Start Latency | < 100ms | Optimized queries, caching |
| Upload Throughput | Multi-Gbps | Pre-signed URLs, direct blob uploads |
| Availability | 99.9% | Circuit breakers, retries, graceful degradation |
| Concurrent Uploads | 10,000+ | Stateless design, horizontal scaling |

## Scalability

### Horizontal Scaling
- **Stateless Design**: No session affinity required
- **Database Partitioning**: Shard by user ID
- **Blob Storage**: Partitioned by upload date/user

### Caching Strategy
- **Redis**: Frequently accessed upload sessions
- **Cache Stampede Protection**: Random jitter on cache misses
- **TTL-based Invalidation**: Automatic expiry

### Load Distribution
- **Round-robin LB**: Even distribution
- **Circuit Breaker**: Isolate failing instances
- **Health Checks**: Automatic instance removal

## Security

### Authentication & Authorization
- **OAuth2 Resource Server**: JWT validation
- **Role-based Access**: USER role required
- **User Ownership**: Verify upload ownership before operations

### Pre-signed URLs
- **Time-limited**: Expire after configured duration
- **Permission-scoped**: Write-only access
- **Validated**: Server-side blob verification

### File Validation
- **Content Type**: Verify MIME type
- **File Size**: Enforce min/max limits
- **Malware Scanning**: Integration with security services

## Monitoring

### Metrics
- Upload initiation rate
- Upload success/failure rate
- P95/P99 latency
- Quota consumption
- Circuit breaker state
- Error rates by type

### Logging
- Structured logging with correlation IDs
- Saga execution traces
- Performance timings
- Security events

### Alerts
- High failure rate
- Circuit breaker opened
- Quota exhaustion
- Slow performance degradation

## Database Schema

### video_upload
- Stores upload sessions and metadata
- Indexed by user_id, status, created_at
- Tracks progress and retry count

### upload_quota
- Daily, weekly, monthly quota tracking
- Automatic period reset
- Upload count limits

### chunk_upload
- Resumable upload support
- Multi-part upload tracking
- Chunk verification

## Deployment

### Container Image
```dockerfile
FROM openjdk:17-jdk-slim
COPY target/video-upload-service.jar app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
```

### Kubernetes Deployment
```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: video-upload-service
spec:
  replicas: 3
  selector:
    matchLabels:
      app: video-upload-service
  template:
    metadata:
      labels:
        app: video-upload-service
    spec:
      containers:
      - name: video-upload
        image: video-upload-service:latest
        resources:
          requests:
            cpu: "500m"
            memory: "1Gi"
          limits:
            cpu: "2000m"
            memory: "2Gi"
```

### Environment Variables
```bash
AZURE_STORAGE_CONNECTION_STRING=...
AZURE_SERVICEBUS_CONNECTION_STRING=...
SPRING_PROFILES_ACTIVE=production
```

## Testing Strategy

### Unit Tests
- Saga step execution
- Compensation logic
- Quota enforcement
- Validation rules

### Integration Tests
- End-to-end upload flow
- Circuit breaker behavior
- Retry mechanisms
- Database operations

### Performance Tests
- Latency benchmarks
- Throughput measurements
- Concurrent upload handling
- Load testing

## Future Enhancements

1. **Resumable Upload API**: Chunked upload support
2. **Progress Webhooks**: Real-time upload status
3. **Video Validation**: Format, codec, resolution checks
4. **Upload Bandwidth Throttling**: Rate limiting
5. **Multi-region Upload**: Azure Front Door routing
6. **AI-powered Quality Checks**: Automatic content review

