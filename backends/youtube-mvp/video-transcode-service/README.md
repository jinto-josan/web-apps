# Video Transcode Service (Unified Media Processing)

## Overview

A unified media processing service that combines video transcoding, thumbnail generation, and DRM packaging using Azure Media Services. This service follows Clean Architecture principles and implements Domain-Driven Design (DDD) patterns.

## Key Features

- **Multi-bitrate Encoding**: Adaptive bitrate streaming with HLS/DASH
- **Thumbnail Generation**: Automatic thumbnail extraction at specified timecodes
- **DRM Protection**: Multi-DRM support (Widevine, FairPlay, PlayReady)
- **High Reliability**: >99.5% job success rate
- **Performance**: P95 queue wait <2 minutes
- **Cost Optimization**: Azure managed services with automatic scaling

## Architecture

### Clean Architecture Layers

```
┌─────────────────────────────────────────┐
│        Interface Layer (REST/Events)    │
├─────────────────────────────────────────┤
│      Application Layer (Use Cases)     │
├─────────────────────────────────────────┤
│         Domain Layer (Entities)        │
├─────────────────────────────────────────┤
│     Infrastructure Layer (Azure AMS)   │
└─────────────────────────────────────────┘
```

### Domain Entities

- `MediaProcessingJob`: Tracks the entire media processing workflow
- `Thumbnail`: Manages video thumbnails with selection capabilities
- `DRMKey`: Stores DRM key metadata and encryption configuration
- `ProcessingStatus`: Enum for job lifecycle states

### Design Patterns

1. **Orchestrator Pattern**: `MediaProcessingOrchestrator` coordinates all processing steps
2. **Repository Pattern**: Data access abstraction for domain entities
3. **Outbox Pattern**: Reliable event publishing with transactional guarantees
4. **Adapter Pattern**: Azure Media Services abstraction layer
5. **Strategy Pattern**: Different DRM and encoding strategies

## Azure Integration

### Azure Media Services

The service uses Azure Media Services (Option A - Managed) for:

- **Transforms**: Define encoding presets and configurations
- **Jobs**: Submit and monitor encoding jobs
- **Assets**: Store and manage encoded content
- **Streaming Endpoints**: Deliver content via HLS/DASH
- **Content Key Policies**: DRM protection and licensing

### Azure Key Vault

Secure storage and management of:
- Content encryption keys
- DRM signing keys
- License encryption keys

### Azure Blob Storage

Persistent storage for:
- Input videos
- Transcoded assets
- Thumbnail images
- Packaged streaming files

## Processing Workflow

### Sequence of Operations

1. **Video Upload Event**: Service receives `VideoUploadedEvent`
2. **Job Creation**: Create `MediaProcessingJob` record
3. **Encoding**: Submit to Azure Media Services for multi-bitrate encoding
4. **Job Monitoring**: Poll job status until completion
5. **Thumbnail Generation**: Extract thumbnails at configured timecodes
6. **Packaging**: Create HLS/DASH manifests for streaming
7. **DRM Protection**: Apply Widevine, FairPlay, PlayReady protection
8. **Event Publishing**: Emit `MediaProcessingCompletedEvent`

### Status Flow

```
QUEUED → ENCODING → THUMBNAIL_GENERATING → PACKAGING → DRM_PROCESSING → COMPLETED
                                                              ↓
                                                           FAILED
```

## API Endpoints

### Media Processing

- `POST /api/media/process` - Start media processing
- `GET /api/media/jobs/{jobId}` - Get job status
- `GET /api/media/jobs/video/{videoId}` - Get jobs for video

### Thumbnails

- `GET /api/thumbnails/video/{videoId}` - List all thumbnails
- `POST /api/thumbnails/select` - Select thumbnail
- `GET /api/thumbnails/{thumbnailId}` - Get thumbnail details

### DRM

- `POST /api/drm/license` - Issue DRM license
- `GET /api/drm/keys/video/{videoId}` - Get DRM keys for video

## Dependencies

### Maven Dependencies

```xml
<!-- Azure Media Services -->
<dependency>
    <groupId>com.azure.resourcemanager</groupId>
    <artifactId>azure-resourcemanager-media</artifactId>
</dependency>

<!-- Azure Blob Storage -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-storage-blob</artifactId>
</dependency>

<!-- Azure Key Vault -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-keys</artifactId>
</dependency>

<!-- Image Processing -->
<dependency>
    <groupId>net.coobird</groupId>
    <artifactId>thumbnailator</artifactId>
</dependency>

<!-- Encryption -->
<dependency>
    <groupId>org.bouncycastle</groupId>
    <artifactId>bcprov-jdk15on</artifactId>
</dependency>
```

## Configuration

### Azure Media Services

```yaml
azure:
  media-services:
    subscription-id: ${AZURE_SUBSCRIPTION_ID}
    resource-group: ${AZURE_RESOURCE_GROUP}
    account-name: ${AZURE_MEDIA_SERVICES_ACCOUNT}
    endpoint: https://${AZURE_MEDIA_SERVICES_ACCOUNT}.rest.media.azure.net
```

### Azure Blob Storage

```yaml
azure:
  storage:
    account-name: ${AZURE_STORAGE_ACCOUNT}
    account-key: ${AZURE_STORAGE_KEY}
    container:
      videos: videos
      thumbnails: thumbnails
      transcoded: transcoded
```

### Azure Key Vault

```yaml
azure:
  key-vault:
    endpoint: https://${AZURE_KEY_VAULT_NAME}.vault.azure.net/
    client-id: ${AZURE_CLIENT_ID}
    client-secret: ${AZURE_CLIENT_SECRET}
```

## Non-Functional Requirements (NFRs)

### Performance

- **Queue Wait (P95)**: < 2 minutes
- **Job Success Rate**: > 99.5%
- **Encoding Time**: Varies by video length and resolution
- **Thumbnail Generation**: < 30 seconds per video

### Cost Optimization

- Use Azure Media Services Reserved Instances
- Implement job queuing to batch process
- Enable automatic scaling based on queue depth
- Use appropriate encoding presets for different content types

### Reliability

- Automatic retry for transient failures
- Circuit breaker pattern for external service calls
- Dead letter queue for failed jobs
- Comprehensive logging and monitoring

## Testing

### Unit Tests

- Domain entities and value objects
- Repository implementations
- Service logic
- Azure adapter mocks

### Integration Tests

- Complete workflow from upload to completion
- Azure Media Services integration
- Event publishing
- Error handling scenarios

### Test Coverage

Run tests with coverage:

```bash
mvn test jacoco:report
```

Target: > 80% code coverage

## Monitoring

### Metrics

- Job completion rate
- Average processing time
- Thumbnail generation success rate
- DRM protection application rate
- Azure API call failures

### Logging

Structured logging with correlation IDs for:
- Job lifecycle events
- Azure Media Services API calls
- Thumbnail generation
- DRM key management
- Event publishing

### Alerts

- Failed job rate > 1%
- Queue depth > 1000
- Average processing time > SLA
- Azure API error rate > 5%

## Deployment

### Docker

```bash
docker build -t video-transcode-service .
docker run -p 8080:8080 video-transcode-service
```

### Kubernetes

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
        image: video-transcode-service:latest
        env:
        - name: AZURE_SUBSCRIPTION_ID
          valueFrom:
            secretKeyRef:
              name: azure-credentials
              key: subscription-id
```

## Troubleshooting

### Common Issues

1. **Encoding Jobs Stuck in QUEUED**: Check Azure Media Services quota
2. **Thumbnail Generation Fails**: Verify FFmpeg/thumbnail service availability
3. **DRM License Errors**: Check Key Vault connectivity and key rotation
4. **High Processing Time**: Review encoding preset for optimization

### Debugging

Enable debug logging:

```yaml
logging:
  level:
    com.youtube.videotranscodeservice: DEBUG
    com.azure.resourcemanager.media: DEBUG
```

## Future Enhancements

- [ ] Support for live streaming encoding
- [ ] Advanced thumbnail selection using ML
- [ ] Custom DRM policies per video
- [ ] Multi-region processing for lower latency
- [ ] Automatic content moderation integration

## References

- [Azure Media Services Documentation](https://docs.microsoft.com/azure/media-services/)
- [Clean Architecture](https://blog.cleancoder.com/uncle-bob/2012/08/13/the-clean-architecture.html)
- [Domain-Driven Design](https://www.domainlanguage.com/ddd/)

