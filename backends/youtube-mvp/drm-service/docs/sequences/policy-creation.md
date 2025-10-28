# DRM Policy Creation Sequence

## Sequence Diagram

```plantuml
@startuml
actor Client
participant Controller
participant UseCase
participant IdempotencyRepo
participant PolicyRepo
participant AmsAdapter
participant CacheService
participant EventPublisher
participant AuditRepo

Client -> Controller: POST /api/v1/drm/policies
Controller -> Controller: Extract userId from JWT
Controller -> Controller: Build CreateDrmPolicyCommand
Controller -> UseCase: createPolicy(command)

alt Idempotency Key provided
    UseCase -> IdempotencyRepo: isIdempotent(key)
    IdempotencyRepo --> UseCase: false
    UseCase -> IdempotencyRepo: markIdempotent(key, 86400)
end

UseCase -> PolicyRepo: existsByVideoId(videoId)
PolicyRepo --> UseCase: false

UseCase -> UseCase: Generate ULID for policyId
UseCase -> UseCase: Build DrmPolicy entity

UseCase -> AmsAdapter: createOrUpdateContentKeyPolicy(provider, config)
AmsAdapter -> AmsAdapter: Call Azure Media Services API
AmsAdapter --> UseCase: amsPolicyId

UseCase -> PolicyRepo: save(policy)
PolicyRepo --> UseCase: savedPolicy

UseCase -> CacheService: putPolicy(policy)

UseCase -> AuditRepo: save(auditLog)

UseCase -> EventPublisher: publishPolicyCreated(event)
EventPublisher -> EventPublisher: Send to Service Bus

UseCase --> Controller: DrmPolicy
Controller -> Controller: Build DrmPolicyResponse
Controller --> Client: 201 Created
@enduml
```

## Flow Description

1. **Request Reception**: Client sends POST request with DRM policy details
2. **Authentication**: Controller extracts user ID from JWT token
3. **Idempotency Check**: If idempotency key provided, check if operation already executed
4. **Validation**: Check if DRM policy already exists for the video
5. **Policy Creation**: Generate unique policy ID and create policy entity
6. **AMS Integration**: Create content key policy in Azure Media Services
7. **Persistence**: Save policy to PostgreSQL database
8. **Caching**: Store policy in Redis cache
9. **Audit Logging**: Create audit log entry for policy creation
10. **Event Publishing**: Publish policy created event to Service Bus
11. **Response**: Return created policy with 201 status

