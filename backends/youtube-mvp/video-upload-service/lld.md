# Low-Level Design (LLD) - Video Upload Service

## Table of Contents
1. [Overview](#overview)
2. [System Architecture](#system-architecture)
3. [Class Diagrams](#class-diagrams)
4. [Database Design](#database-design)
5. [API Design](#api-design)
6. [Saga Orchestration](#saga-orchestration)
7. [Error Handling](#error-handling)
8. [Performance Considerations](#performance-considerations)
9. [Security Design](#security-design)
10. [Testing Strategy](#testing-strategy)

## Overview

### Purpose
The Video Upload Service enables users to upload video files efficiently with support for:
- Pre-signed URLs for direct client uploads
- Resumable chunked uploads
- Upload validation and quota management
- Saga-based distributed transaction orchestration

### Key Requirements

#### Functional Requirements
1. **FR1**: Initialize video upload with pre-signed URL generation
2. **FR2**: Check upload status and progress
3. **FR3**: Cancel active uploads
4. **FR4**: Enforce upload quotas (daily/weekly/monthly)
5. **FR5**: Validate upload files (size, type, format)

#### Non-Functional Requirements
1. **NFR1**: P95 start latency < 100ms
2. **NFR2**: Support multi-Gbps upload throughput
3. **NFR3**: 99.9% availability
4. **NFR4**: Support 10,000+ concurrent uploads
5. **NFR5**: Automatic retry with exponential backoff

## System Architecture

### Component Overview

```
┌─────────────────────────────────────────────────────────────┐
│                      Client Layer                            │
│  (Browser/Mobile App - Direct Blob Upload)                  │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    Interface Layer                           │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ VideoUploadController                                 │   │
│  │  - initializeUpload()                                 │   │
│  │  - getUploadStatus()                                  │   │
│  │  - cancelUpload()                                      │   │
│  └──────────────────────────────────────────────────────┘   │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                  Application Layer                           │
│  ┌──────────────────────────────────────────────────────┐   │
│  │ InitializeUploadSaga                                  │   │
│  │  ├─ ValidateRequestStep                               │   │
│  │  ├─ CheckQuotaStep                                    │   │
│  │  ├─ GeneratePreSignedUrlStep                         │   │
│  │  └─ CreateUploadSessionStep                           │   │
│  └──────────────────────────────────────────────────────┘   │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                    Domain Layer                               │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐         │
│  │ VideoUpload │  │ChunkUpload   │  │UploadQuota   │         │
│  │   Entity    │  │   Entity     │  │   Entity    │         │
│  └─────────────┘  └─────────────┘  └─────────────┘         │
│                                                               │
│  ┌─────────────┐  ┌─────────────┐                            │
│  │BlobStorage  │  │VideoValidator│                           │
│  │  Service    │  │   Service    │                          │
│  └─────────────┘  └─────────────┘                            │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│               Infrastructure Layer                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │ Azure Blob  │  │  Repository  │  │  Security  │          │
│  │  Storage    │  │ Implementations│ │  Config    │          │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
└────────────────────┬────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                  External Services                            │
│  ┌─────────────┐  ┌─────────────┐  ┌─────────────┐          │
│  │Azure Blob   │  │Azure Service│  │   PostgreSQL│         │
│  │  Storage    │  │     Bus      │  │   Database  │         │
│  └─────────────┘  └─────────────┘  └─────────────┘          │
└───────────────────────────────────────────────────────────────┘
```

## Class Diagrams

### Domain Entities

```plantuml
@startuml DomainEntities

class VideoUpload {
    -String id
    -String userId
    -String channelId
    -String videoTitle
    -String videoDescription
    -UploadStatus status
    -Long totalSizeBytes
    -Long uploadedSizeBytes
    -String blobName
    -String blobContainer
    -String contentType
    -String etag
    -Instant createdAt
    -Instant updatedAt
    -Instant expiresAt
    -Integer expirationMinutes
    -String errorMessage
    -Integer retryCount
    -Integer maxRetries
    +double getProgressPercentage()
    +boolean isComplete()
    +boolean isResumable()
    +void markAsFailed(String error)
}

enum UploadStatus {
    INITIALIZING
    UPLOADING
    UPLOAD_COMPLETE
    VALIDATING
    VALIDATION_COMPLETE
    TRANSCODE_QUEUED
    FAILED
    CANCELLED
    EXPIRED
}

class ChunkUpload {
    -String id
    -String uploadId
    -Integer chunkNumber
    -Integer totalChunks
    -Long chunkSizeBytes
    -Long chunkStartByte
    -Long chunkEndByte
    -ChunkStatus status
    -String etag
    -String preSignedUrl
    -Instant expiresAt
    -Instant uploadedAt
    -String errorMessage
    +double getProgressPercentage()
    +void markAsCompleted(String etag, Instant uploadedAt)
}

class UploadQuota {
    -String userId
    -QuotaType quotaType
    -Long currentUsage
    -Long quotaLimit
    -Instant periodStart
    -Instant periodEnd
    -Integer uploadCount
    -Integer uploadLimit
    +boolean isExceeded()
    +long getRemainingQuota()
    +boolean isExpired()
    +void consumeQuota(long sizeBytes)
    +void releaseQuota(long sizeBytes)
    +void resetQuota(Instant periodStart, Instant periodEnd)
}

enum QuotaType {
    DAILY
    WEEKLY
    MONTHLY
    LIFETIME
}

class PreSignedUrl {
    -String url
    -Instant expiresAt
    -String uploadId
    -String blobName
    -String containerName
    -Long maxFileSizeBytes
    -Integer durationMinutes
    +boolean isExpired()
    +long getRemainingSeconds()
}

VideoUpload --> UploadStatus
ChunkUpload --> ChunkStatus
UploadQuota --> QuotaType

@enduml
```

### Application Layer - Saga Pattern

```plantuml
@startuml SagaPattern

interface Saga<T> {
    +T execute() throws SagaExecutionException
    +String getSagaId()
    +String getSagaType()
    +List<SagaStep> getSteps()
}

interface SagaStep {
    +Object execute(SagaContext context) throws SagaStepException
    +void compensate(SagaContext context) throws SagaStepException
    +String getStepName()
    +boolean isCompensatable()
}

class SagaContext {
    -Map<String, Object> data
    -String sagaId
    -String sagaType
    +void put(String key, Object value)
    +<T> T get(String key, Class<T> type)
    +boolean containsKey(String key)
}

class InitializeUploadSaga implements Saga<PreSignedUrl> {
    -String sagaId
    -String userId
    -String channelId
    -String videoTitle
    -String videoDescription
    -long fileSizeBytes
    -String contentType
    -int expirationMinutes
    -VideoUploadRepository uploadRepo
    -UploadQuotaRepository quotaRepo
    -BlobStorageService blobService
    -VideoValidator validator
    +PreSignedUrl execute() throws SagaExecutionException
    -void compensate(SagaContext context, String failedStep)
    -ValidateRequestStep
    -CheckQuotaStep
    -GeneratePreSignedUrlStep
    -CreateUploadSessionStep
}

class ValidateRequestStep implements SagaStep {
    +Object execute(SagaContext context)
    +void compensate(SagaContext context)
    +String getStepName()
    +boolean isCompensatable()
}

class CheckQuotaStep implements SagaStep {
    +Object execute(SagaContext context)
    +void compensate(SagaContext context)
    +String getStepName()
    +boolean isCompensatable()
}

class GeneratePreSignedUrlStep implements SagaStep {
    +Object execute(SagaContext context)
    +void compensate(SagaContext context)
    +String getStepName()
    +boolean isCompensatable()
}

class CreateUploadSessionStep implements SagaStep {
    +Object execute(SagaContext context)
    +void compensate(SagaContext context)
    +String getStepName()
    +boolean isCompensatable()
}

InitializeUploadSaga --> Saga
ValidateRequestStep --> SagaStep
CheckQuotaStep --> SagaStep
GeneratePreSignedUrlStep --> SagaStep
CreateUploadSessionStep --> SagaStep
InitializeUploadSaga ..> ValidateRequestStep : contains
InitializeUploadSaga ..> CheckQuotaStep : contains
InitializeUploadSaga ..> GeneratePreSignedUrlStep : contains
InitializeUploadSaga ..> CreateUploadSessionStep : contains

@enduml
```

### Repository Pattern

```plantuml
@startuml RepositoryPattern

interface VideoUploadRepository {
    +VideoUpload save(VideoUpload upload)
    +Optional<VideoUpload> findById(String uploadId)
    +List<VideoUpload> findByUserId(String userId)
    +List<VideoUpload> findActiveUploads(String userId)
    +List<VideoUpload> findByStatus(UploadStatus status)
    +void updateStatus(String uploadId, UploadStatus status)
    +void updateProgress(String uploadId, long uploadedSizeBytes)
    +void delete(String uploadId)
    +boolean exists(String uploadId)
    +List<VideoUpload> findExpiredUploads()
}

interface UploadQuotaRepository {
    +UploadQuota getOrCreate(String userId, QuotaType quotaType)
    +Optional<UploadQuota> findByUserIdAndType(String userId, QuotaType quotaType)
    +UploadQuota save(UploadQuota quota)
    +void consumeQuota(String userId, long sizeBytes, QuotaType quotaType)
    +void releaseQuota(String userId, long sizeBytes, QuotaType quotaType)
    +boolean hasRemainingQuota(String userId, long sizeBytes, QuotaType quotaType)
    +long getRemainingQuota(String userId, QuotaType quotaType)
}

interface BlobStorageService {
    +PreSignedUrl generatePreSignedUrl(...)
    +Map<Integer, PreSignedUrl> generateChunkUrls(...)
    +boolean verifyBlobComplete(String containerName, String blobName)
    +long getBlobSize(String containerName, String blobName)
    +String getBlobEtag(String containerName, String blobName)
    +boolean deleteBlob(String containerName, String blobName)
    +Map<String, Long> listBlobs(String containerName, String prefix)
}

class VideoUploadRepositoryJPA implements VideoUploadRepository {
    -VideoUploadEntityRepository jpaRepo
    +VideoUpload save(VideoUpload upload)
    +Optional<VideoUpload> findById(String uploadId)
    +VideoUploadEntity toEntity(VideoUpload upload)
    +VideoUpload toDomain(VideoUploadEntity entity)
}

class AzureBlobStorageService implements BlobStorageService {
    -BlobServiceClient blobServiceClient
    -String connectionString
    +PreSignedUrl generatePreSignedUrl(...)
    +Map<Integer, PreSignedUrl> generateChunkUrls(...)
}

VideoUploadRepositoryJPA ..|> VideoUploadRepository
AzureBlobStorageService ..|> BlobStorageService

@enduml
```

## Database Design

### Entity-Relationship Diagram

```plantuml
@startuml DatabaseSchema

entity "video_upload" as VideoUpload {
    * id VARCHAR(255) PK
    --
    * user_id VARCHAR(255) FK
    channel_id VARCHAR(255)
    video_title VARCHAR(500)
    video_description TEXT
    * status VARCHAR(50)
    * total_size_bytes BIGINT
    uploaded_size_bytes BIGINT
    blob_name VARCHAR(1000)
    blob_container VARCHAR(255)
    content_type VARCHAR(100)
    etag VARCHAR(255)
    * created_at TIMESTAMP
    * updated_at TIMESTAMP
    expires_at TIMESTAMP
    expiration_minutes INTEGER
    error_message TEXT
    retry_count INTEGER
    max_retries INTEGER
}

entity "upload_quota" as UploadQuota {
    * id BIGSERIAL PK
    --
    * user_id VARCHAR(255) FK
    * quota_type VARCHAR(50)
    current_usage BIGINT
    * quota_limit BIGINT
    period_start TIMESTAMP
    * period_end TIMESTAMP
    upload_count INTEGER
    upload_limit INTEGER
}

entity "chunk_upload" as ChunkUpload {
    * id VARCHAR(255) PK
    --
    * upload_id VARCHAR(255) FK
    * chunk_number INTEGER
    total_chunks INTEGER
    chunk_size_bytes BIGINT
    chunk_start_byte BIGINT
    chunk_end_byte BIGINT
    status VARCHAR(50)
    etag VARCHAR(255)
    pre_signed_url TEXT
    expires_at TIMESTAMP
    uploaded_at TIMESTAMP
    error_message TEXT
}

VideoUpload ||--o{ ChunkUpload : "has"
VideoUpload }o--|| UploadQuota : "tracked by"

note right of VideoUpload
    Indexes:
    - idx_user_id (user_id)
    - idx_status (status)
    - idx_created_at (created_at)
    - idx_expires_at (expires_at)
    - idx_channel_id (channel_id)
end note

note right of UploadQuota
    Indexes:
    - idx_user_id_type (user_id, quota_type)
    
    Unique:
    - unique_user_quota_type
end note

note right of ChunkUpload
    Indexes:
    - idx_upload_id (upload_id)
    - idx_chunk_number (upload_id, chunk_number)
end note

@enduml
```

### Schema Details

#### Table: video_upload
**Purpose**: Stores video upload sessions and metadata

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | VARCHAR(255) | PRIMARY KEY | Unique upload session ID |
| user_id | VARCHAR(255) | NOT NULL, FK | Owner of the upload |
| channel_id | VARCHAR(255) | NULL | Target channel |
| status | VARCHAR(50) | NOT NULL | Upload status (enum) |
| total_size_bytes | BIGINT | NOT NULL | Total file size |
| uploaded_size_bytes | BIGINT | DEFAULT 0 | Uploaded bytes |
| blob_name | VARCHAR(1000) | NULL | Azure blob name |
| content_type | VARCHAR(100) | NULL | MIME type |
| created_at | TIMESTAMP | NOT NULL | Creation timestamp |
| updated_at | TIMESTAMP | NOT NULL | Last update timestamp |
| expires_at | TIMESTAMP | NULL | URL expiration time |
| retry_count | INTEGER | DEFAULT 0 | Number of retry attempts |

**Indexes:**
- `idx_user_id`: For finding all uploads by user
- `idx_status`: For finding uploads by status
- `idx_created_at`: For chronological queries
- `idx_expires_at`: For cleanup of expired uploads

#### Table: upload_quota
**Purpose**: Tracks user upload quotas and consumption

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGSERIAL | PRIMARY KEY | Auto-increment ID |
| user_id | VARCHAR(255) | NOT NULL, FK | Quota owner |
| quota_type | VARCHAR(50) | NOT NULL | Type (DAILY/WEEKLY/MONTHLY) |
| current_usage | BIGINT | DEFAULT 0 | Bytes used in period |
| quota_limit | BIGINT | NOT NULL | Maximum allowed bytes |
| period_start | TIMESTAMP | NULL | Period start time |
| period_end | TIMESTAMP | NOT NULL | Period end time |
| upload_count | INTEGER | DEFAULT 0 | Number of uploads |
| upload_limit | INTEGER | DEFAULT 20 | Max uploads per period |

**Indexes:**
- `idx_user_id_type`: Composite index for fast quota lookups
- `unique_user_quota_type`: Ensures one quota record per user+type

#### Table: chunk_upload
**Purpose**: Manages chunked/resumable uploads

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | VARCHAR(255) | PRIMARY KEY | Unique chunk ID |
| upload_id | VARCHAR(255) | NOT NULL, FK | Parent upload |
| chunk_number | INTEGER | NOT NULL | Chunk index (0-based) |
| chunk_size_bytes | BIGINT | NULL | Chunk size |
| status | VARCHAR(50) | NULL | Chunk status |
| etag | VARCHAR(255) | NULL | Azure blob ETag |
| pre_signed_url | TEXT | NULL | URL for this chunk |
| expires_at | TIMESTAMP | NULL | URL expiration |

## API Design

### Endpoints

#### 1. Initialize Upload

**Request:**
```http
POST /api/v1/uploads/initialize
Authorization: Bearer <jwt-token>
Content-Type: application/json

{
  "channelId": "channel-123",
  "title": "My Video",
  "description": "Video description",
  "fileSizeBytes": 524288000,
  "contentType": "video/mp4",
  "expirationMinutes": 60
}
```

**Response (200 OK):**
```json
{
  "uploadId": "uuid-123",
  "preSignedUrl": "https://storage.azure.com/container/blob?token=...",
  "expiresAt": "2024-01-01T12:00:00Z",
  "blobName": "uploads/user-123/uuid.mp4",
  "durationMinutes": 60
}
```

**Error Responses:**
- `400 Bad Request`: Invalid request payload
- `401 Unauthorized`: Missing or invalid JWT
- `403 Forbidden`: Insufficient quota
- `500 Internal Server Error`: System error

#### 2. Get Upload Status

**Request:**
```http
GET /api/v1/uploads/{uploadId}/status
Authorization: Bearer <jwt-token>
```

**Response (200 OK):**
```json
{
  "uploadId": "uuid-123",
  "status": "UPLOADING",
  "progressPercentage": 45.5,
  "totalSizeBytes": 524288000,
  "uploadedSizeBytes": 238553600,
  "errorMessage": null
}
```

**Status Values:**
- `INITIALIZING`: Upload session created, awaiting client
- `UPLOADING`: Active upload in progress
- `UPLOAD_COMPLETE`: Upload finished, validation pending
- `VALIDATION_COMPLETE`: Ready for transcode
- `FAILED`: Upload or validation failed
- `CANCELLED`: User cancelled upload
- `EXPIRED`: Pre-signed URL expired

#### 3. Cancel Upload

**Request:**
```http
POST /api/v1/uploads/{uploadId}/cancel
Authorization: Bearer <jwt-token>
```

**Response (200 OK):**
```http
HTTP/1.1 200 OK
```

**Side Effects:**
- Quota released
- Blob deleted from Azure
- Session marked as cancelled

## Saga Orchestration

### Saga Flow Diagram

```plantuml
@startuml SagaFlow
!theme plain
skinparam backgroundColor #FFFFFF

start

:Initiate Saga;

:Step 1: Validate Request;
note right
  - Check file size
  - Validate content type
  - No compensation
end note

if (Valid?) then (yes)
  :Step 2: Check Quota;
  note right
    - Verify remaining quota
    - Consume quota
    - Compensation: Release quota
  end note
  
  if (Quota Available?) then (yes)
    :Step 3: Generate Pre-signed URL;
    note right
      - Create Azure SAS token
      - Build pre-signed URL
      - No compensation needed
    end note
    
    if (URL Generated?) then (yes)
      :Step 4: Create Upload Session;
      note right
        - Persist upload metadata
        - Compensation: Delete session
      end note
      
      if (Session Created?) then (yes)
        :Return Pre-signed URL;
        stop
      else (no)
        :Execute Compensation;
        :Release Quota;
        :Return Error;
        stop
      endif
    else (no)
      :Execute Compensation;
      :Release Quota;
      :Return Error;
      stop
    endif
  else (no)
    :Return Quota Error;
    stop
  endif
else (no)
  :Return Validation Error;
  stop
endif

@enduml
```

### Compensation Logic

| Step | Compensation Action | Compensatable |
|------|-------------------|----------------|
| Validate Request | None (read-only) | No |
| Check Quota | Release consumed quota | Yes |
| Generate Pre-signed URL | None (idempotent) | No |
| Create Upload Session | Delete upload record | Yes |

## Error Handling

### Error Categories

1. **Validation Errors** (400): Invalid input
   - File size out of range
   - Unsupported content type
   - Missing required fields

2. **Quota Errors** (403): Resource limits exceeded
   - Daily quota exceeded
   - Upload count limit reached

3. **Infrastructure Errors** (500): System failures
   - Azure Blob Storage unavailable
   - Database connection failures
   - Network timeouts

4. **Authorization Errors** (401/403): Security violations
   - Invalid JWT token
   - Unauthorized access attempt

### Resilience Strategies

1. **Retry Logic**: Exponential backoff for transient failures
2. **Circuit Breaker**: Fail-fast for persistent failures
3. **Timeout Handling**: Prevent hanging operations
4. **Graceful Degradation**: Degrade functionality rather than fail completely

## Performance Considerations

### Query Optimization

1. **Use Indexes**: All foreign keys and frequently queried columns indexed
2. **Pagination**: Limit result sets for list queries
3. **Connection Pooling**: Efficient database connection management
4. **Caching**: Cache frequently accessed data (Redis)

### Latency Targets

| Operation | P50 | P95 | P99 |
|-----------|-----|-----|-----|
| Initialize Upload | 50ms | 100ms | 200ms |
| Get Status | 10ms | 20ms | 50ms |
| Cancel Upload | 100ms | 200ms | 500ms |

### Scalability

1. **Horizontal Scaling**: Stateless design allows unlimited scaling
2. **Read Replicas**: Separate read/write databases
3. **CDN**: Cache static content
4. **Async Processing**: Background job processing

## Security Design

### Authentication
- OAuth2 Resource Server with JWT tokens
- Token validation on every request
- User ID extraction from JWT subject claim

### Authorization
- Role-based access control (RBAC)
- User ownership verification
- Quota enforcement per user

### Data Protection
- Pre-signed URLs with time-limited access
- SAS tokens with write-only permissions
- HTTPS/TLS for all communications
- Secure credential storage

## Testing Strategy

### Unit Tests
- Saga step execution and compensation
- Repository operations
- Quota enforcement logic
- Validation rules

### Integration Tests
- End-to-end upload flow
- Saga orchestration
- Azure Blob Storage integration
- Database operations

### Performance Tests
- Latency benchmarks
- Throughput measurements
- Concurrent upload handling
- Load testing with 10,000+ concurrent requests

### Security Tests
- Authentication bypass attempts
- Authorization failures
- SQL injection attempts
- XSS and CSRF attacks

