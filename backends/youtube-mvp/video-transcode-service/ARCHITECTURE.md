# Video Transcode Service - Unified Media Processing Architecture

## Executive Summary

The Video Transcode Service is a unified media processing service that combines video transcoding, thumbnail generation, and DRM packaging into a single, cohesive system using Azure Media Services. The service follows Clean Architecture principles and implements Domain-Driven Design (DDD) patterns to ensure maintainability, testability, and scalability.

## Architecture Overview

### High-Level Design

```
┌─────────────────────────────────────────────────────────────┐
│                    Video Transcode Service                    │
│                  (Unified Media Processing)                  │
└─────────────────────────────────────────────────────────────┘
                             │
                             ├─── Multi-bitrate Encoding (HLS/DASH)
                             ├─── Thumbnail Generation
                             └─── DRM Protection (Widevine, FairPlay, PlayReady)
                                        │
                                        ▼
┌─────────────────────────────────────────────────────────────┐
│                   Azure Media Services                      │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │ Transforms   │  │   Jobs API   │  │   Assets API │       │
│  │  Presets     │  │   Monitoring │  │   Content   │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
│                                                               │
│  ┌──────────────┐  ┌──────────────┐  ┌──────────────┐       │
│  │  Streaming   │  │ Content Key  │  │ DRM License │       │
│  │   Endpoint   │  │   Policies   │  │   Server    │       │
│  └──────────────┘  └──────────────┘  └──────────────┘       │
└─────────────────────────────────────────────────────────────┘
```

## Functional Requirements

### Core Capabilities

1. **Multi-bitrate Video Encoding**
   - Adaptive bitrate streaming (HLS/DASH)
   - Multiple quality profiles: 1080p, 720p, 480p, 360p, 240p
   - Automatic codec optimization (H.264, H.265)
   - Segment duration: 6-10 seconds for optimal buffering

2. **Thumbnail Generation**
   - Automatic extraction at configurable timecodes (e.g., 00:00:03, 00:00:10, 00:00:20)
   - Multiple sizes: Small (128x72), Medium (320x180), Large (640x360)
   - Format support: JPEG, WebP
   - User selection tracking

3. **DRM Protection**
   - Multi-DRM support:
     - **Widevine**: Google Chrome, Android
     - **FairPlay**: Apple Safari, iOS
     - **PlayReady**: Edge, Xbox
   - Content encryption at rest and in transit
   - License server integration
   - Key rotation support

## Non-Functional Requirements (NFRs)

### Performance
- **Queue Wait (P95)**: < 2 minutes
- **Job Success Rate**: > 99.5%
- **Processing Time**: Variable based on video length and resolution
- **Thumbnail Generation**: < 30 seconds per video
- **DRM Protection**: < 60 seconds per video

### Scalability
- Horizontal scaling: 3-10 instances based on queue depth
- Azure Media Services auto-scaling
- Blob Storage partitioned by date/video ID
- Connection pooling for Azure clients

### Cost Optimization
- Azure Reserved Instances for consistent workload
- Job queuing to batch process efficiently
- Automatic scaling based on queue depth
- Appropriate encoding presets per content type

### Reliability
- Automatic retry for transient failures (exponential backoff)
- Circuit breaker pattern for external calls
- Dead letter queue for failed jobs
- Comprehensive logging and monitoring

### Security
- Encryption at rest (Azure Key Vault)
- Encryption in transit (TLS 1.3)
- Managed identity for Azure services
- Content key rotation support
- Audit logging for DRM license issuance

## Architectural Patterns

### 1. Clean Architecture

```
┌────────────────────────────────────────┐
│        Interface Layer                 │
│  ┌──────────────┐  ┌──────────────┐   │
│  │  REST API    │  │   Events     │   │
│  └──────────────┘  └──────────────┘   │
└────────────────────────────────────────┘
                  │
┌────────────────────────────────────────┐
│      Application Layer (Use Cases)    │
│  ┌──────────────────────────────────┐  │
│  │  MediaProcessingOrchestrator     │  │
│  └──────────────────────────────────┘  │
└────────────────────────────────────────┘
                  │
┌────────────────────────────────────────┐
│        Domain Layer (Entities)         │
│  ┌──────────────┐  ┌──────────────┐   │
│  │      Job     │  │  Thumbnail   │   │
│  ├──────────────┤  ├──────────────┤   │
│  │   Repository │  │   Repository │   │
│  └──────────────┘  └──────────────┘   │
└────────────────────────────────────────┘
                  │
┌────────────────────────────────────────┐
│    Infrastructure Layer (Azure AMS)   │
│  ┌──────────────────────────────────┐  │
│  │  AzureMediaServicesAdapter       │  │
│  ├──────────────────────────────────┤  │
│  │  AzureBlobStorageClient          │  │
│  ├──────────────────────────────────┤  │
│  │  AzureKeyVaultClient              │  │
│  └──────────────────────────────────┘  │
└────────────────────────────────────────┘
```

### 2. Orchestrator Pattern

The `MediaProcessingOrchestrator` coordinates the entire media processing workflow:

1. **Job Creation**: Create job record with initial state
2. **Encoding**: Submit to Azure Media Services
3. **Monitoring**: Poll job status until completion
4. **Thumbnail Generation**: Extract thumbnails at configured positions
5. **Packaging**: Create HLS/DASH manifests
6. **DRM Protection**: Apply encryption and key management
7. **Event Publishing**: Emit completion events

### 3. Repository Pattern

Abstract data access layer:

- `MediaProcessingRepository`: Job persistence and state management
- `ThumbnailRepository`: Thumbnail CRUD operations
- `DRMKeyRepository`: DRM key metadata storage

### 4. Outbox Pattern

Reliable event publishing with transactional guarantees:

```java
@Transactional
public void processJob(Job job) {
    // 1. Update job status
    repository.save(job);
    
    // 2. Write event to outbox table
    outboxRepository.save(event);
    
    // 3. Commit transaction
    // 4. Background worker publishes events to Service Bus
}
```

### 5. Adapter Pattern

Azure service abstractions:

- `AzureMediaServicesAdapter`: Encoding, packaging, DRM
- `BlobStorageClient`: Asset storage and retrieval
- `KeyVaultClient`: Key management
- `StreamingEndpointClient`: Streaming URL generation

### 6. Strategy Pattern

Different strategies for:
- **Encoding**: Quality presets, codec selection
- **DRM**: Widevine vs FairPlay vs PlayReady
- **Thumbnail**: Timecode vs intelligent sampling

## Data Flow

### Processing Workflow

```
1. VideoUploadedEvent Received
   ├─> MediaProcessingJob Created (QUEUED)
   └─> Submit to Azure Media Services

2. Encoding Job Processing
   ├─> Status: ENCODING → PROCESSING
   ├─> Azure Media Services processes video
   └─> Job completes, get asset ID

3. Thumbnail Generation
   ├─> Extract frames at timecodes
   ├─> Store in Blob Storage
   └─> Create Thumbnail entities

4. Packaging (HLS/DASH)
   ├─> Create manifests
   ├─> Generate streaming URLs
   └─> Update job metadata

5. DRM Protection
   ├─> Generate content keys
   ├─> Apply encryption
   ├─> Create license URLs
   └─> Store keys in Key Vault

6. Event Publishing
   ├─> MediaProcessingCompletedEvent
   ├─> ThumbnailGeneratedEvent
   └─> Update downstream services
```

## Azure Service Integration

### Azure Media Services

**Use Cases:**
- Video encoding with adaptive bitrate
- HLS/DASH packaging
- DRM content protection
- Streaming endpoint management

**Key APIs:**
```java
// Create Transform
Transform transform = mediaClient.transforms().define(transformName)
    .withExistingMediaService(resourceGroup, accountName)
    .withOutputs(outputs)
    .create();

// Submit Job
Job job = mediaClient.jobs().define(jobName)
    .withExistingTransform(transformId)
    .withInputAsset(inputAssetId)
    .create();

// Monitor Job
JobStatus status = job.state();
```

### Azure Blob Storage

**Containers:**
- `videos-input`: Original uploaded videos
- `videos-transcoded`: Encoded assets
- `thumbnails`: Generated thumbnails
- `manifests`: HLS/DASH manifests

### Azure Key Vault

**Secrets:**
- Content encryption keys
- DRM signing keys
- License encryption keys
- API credentials

### Azure Service Bus

**Topics:**
- `media-processing-events`: Job lifecycle events
- `thumbnail-events`: Thumbnail generation events
- `drm-events`: DRM license events

## Implementation Details

### Job Lifecycle

```java
public enum ProcessingStatus {
    QUEUED,              // Job created, waiting to start
    ENCODING,            // Submitted to Azure, encoding in progress
    THUMBNAIL_GENERATING,// Generating thumbnails
    PACKAGING,           // Creating HLS/DASH manifests
    DRM_PROCESSING,      // Applying DRM protection
    COMPLETED,           // All processing complete
    FAILED               // Processing failed
}
```

### Event Types

```java
// Incoming Events
VideoUploadedEvent {
    videoId, userId, inputBlobUrl, timestamp
}

// Outgoing Events
MediaProcessingCompletedEvent {
    videoId, assetId, hlsUrl, dashUrl, thumbnails, drmConfig
}

ThumbnailGeneratedEvent {
    videoId, thumbnailUrls
}

MediaProcessingFailedEvent {
    videoId, errorMessage
}
```

### Error Handling

**Retry Strategy:**
- Transient failures: Exponential backoff (1s, 2s, 4s, 8s)
- Maximum retries: 3
- Circuit breaker: Open after 5 consecutive failures
- Dead letter queue: Failed jobs after max retries

**Error Types:**
- `EncodingFailureException`: Azure Media Services job failed
- `ThumbnailGenerationException`: Frame extraction failed
- `DRMProtectionException`: Key management failed
- `PackagingException`: Manifest generation failed

## Testing Strategy

### Unit Tests
- Domain entities and value objects
- Repository implementations
- Service orchestration logic
- Azure adapter mocks

### Integration Tests
- Complete end-to-end workflow
- Azure Media Services integration
- Event publishing
- Database transactions

### Performance Tests
- Load testing: 100 concurrent jobs
- Queue depth: 1000 pending jobs
- Azure API rate limits
- Blob storage throughput

### Security Tests
- Key vault access control
- DRM license validation
- Content encryption verification
- Audit log compliance

## Deployment

### Environment Variables

```bash
# Azure Credentials
AZURE_SUBSCRIPTION_ID=
AZURE_RESOURCE_GROUP=
AZURE_MEDIA_SERVICES_ACCOUNT=
AZURE_STORAGE_ACCOUNT=
AZURE_KEY_VAULT_NAME=

# Service Configuration
QUEUE_MAX_DEPTH=1000
JOB_TIMEOUT_MINUTES=60
THUMBNAIL_POSITIONS=00:00:03,00:00:10,00:00:20
```

### Docker Compose

```yaml
version: '3.8'
services:
  video-transcode-service:
    image: video-transcode-service:latest
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=production
      - AZURE_SUBSCRIPTION_ID=${AZURE_SUBSCRIPTION_ID}
    volumes:
      - ./logs:/app/logs
```

### Kubernetes Deployment

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: video-transcode-service
spec:
  replicas: 3
  template:
    spec:
      containers:
      - name: video-transcode-service
        image: acr.azure.io/video-transcode-service:latest
        env:
        - name: AZURE_SUBSCRIPTION_ID
          valueFrom:
            secretKeyRef:
              name: azure-credentials
              key: subscription-id
        resources:
          requests:
            memory: "2Gi"
            cpu: "1000m"
          limits:
            memory: "4Gi"
            cpu: "2000m"
```

## Monitoring and Observability

### Metrics

```
# Job Metrics
job_submitted_total
job_completed_total
job_failed_total
job_duration_seconds

# Processing Metrics
encoding_duration_seconds
thumbnail_generation_duration_seconds
packaging_duration_seconds
drm_processing_duration_seconds

# Azure API Metrics
azure_media_services_api_calls_total
azure_media_services_api_errors_total
azure_blob_storage_operations_total
```

### Logging

**Structured Logging:**
```json
{
  "timestamp": "2024-01-17T10:30:00Z",
  "level": "INFO",
  "service": "video-transcode-service",
  "correlationId": "abc123",
  "event": "MediaProcessingStarted",
  "jobId": "job-456",
  "videoId": "video-789",
  "status": "QUEUED"
}
```

### Alerts

- Failed job rate > 1%
- Queue depth > 1000
- Average processing time > SLA
- Azure API error rate > 5%
- Memory usage > 80%

## Future Enhancements

1. **AI-Powered Optimization**
   - Intelligent thumbnail selection using ML
   - Content-aware encoding presets
   - Quality analysis

2. **Advanced Features**
   - Live streaming encoding
   - Multi-region processing
   - Custom DRM policies per video
   - Automatic content moderation

3. **Performance Improvements**
   - GPU-accelerated encoding
   - Pre-computed thumbnails
   - CDN integration for faster delivery

4. **Cost Optimization**
   - Spot instances for non-critical jobs
   - Automatic encoding preset selection based on content
   - Efficient storage lifecycle management

## References

- [Azure Media Services Documentation](https://docs.microsoft.com/azure/media-services/)
- [Clean Architecture - Robert C. Martin](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Domain-Driven Design - Eric Evans](https://www.domainlanguage.com/ddd/)

