# Sequence Diagram: Playback Service Requesting Signed URL

## Overview

This diagram shows the flow when a playback service requests a signed URL for media content.

## PlantUML Diagram

```plantuml
@startuml
actor PlaybackService as PS
participant "Media Controller" as MC
participant "Media Access Use Case" as UC
participant "Idempotency Repo" as IR
participant "Blob Storage Service" as BSS
participant "Azure Blob Storage" as ABS
participant "Audit Log Repo" as ALR

PS -> MC: GET /api/v1/media/origin/{path}
activate MC

MC -> MC: Extract JWT claims (userId)

MC -> UC: generateSignedUrl(request)
activate UC

UC -> IR: retrieve(idempotencyKey)
IR -> IR: Check Redis cache
IR --> UC: cached or empty

alt Cache Hit
    UC -> UC: Parse cached response
    UC --> MC: Return cached URL
else Cache Miss
    UC -> UC: Validate path (prevent traversal)
    UC -> BSS: generatePlaybackUrl(blobPath, policy)
    activate BSS
    
    BSS -> BSS: Create SAS policy\n(validity, permissions)
    BSS -> ABS: Generate SAS signature
    ABS --> BSS: Return SAS token
    
    BSS -> BSS: Build signed URL
    BSS --> UC: Signed URL with expiry
    deactivate BSS
    
    UC -> IR: store(idempotencyKey, response, ttl)
    activate IR
    IR -> IR: Write to Redis
    IR --> UC: Stored
    deactivate IR
end

UC -> ALR: log(AuditEvent)
activate ALR
ALR -> ALR: Publish to Service Bus
ALR --> UC: Logged
deactivate ALR

UC --> MC: Response (url, expiresAt)
deactivate UC

MC --> PS: 200 OK\n{"url": "...", "expiresAt": "..."}
deactivate MC

@enduml
```

## Sequence Description

1. **Playback Service Request**: Playback service makes authenticated request with blob path
2. **Authentication**: Controller validates JWT token and extracts user ID
3. **Idempotency Check**: Service checks for cached response using Idempotency-Key
4. **Path Validation**: Normalize and validate path to prevent directory traversal
5. **SAS Generation**: Generate playback-optimized SAS URL with extended validity
6. **Cache Result**: Store generated URL in Redis for idempotency
7. **Audit Logging**: Publish audit event to Service Bus for compliance
8. **Return Signed URL**: Return signed URL to playback service

## Security Considerations

- **Path Traversal Protection**: All paths are normalized and validated
- **HTTPS Only**: SAS URLs enforce HTTPS in production
- **Audit Trail**: All access events are logged
- **Idempotency**: Prevents duplicate operations
- **Token Expiry**: URLs have configurable expiration

 viewed/allowed

