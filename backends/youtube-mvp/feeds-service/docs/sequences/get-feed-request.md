# Get Feed Request Sequence

```plantuml
@startuml
participant Client
participant "API Gateway/APIM" as APIM
participant "Feeds Controller" as Controller
participant "GetFeedUseCase" as UseCase
participant "FeedCacheService" as Cache
participant "VideoRepositoryAdapter" as VideoRepo
participant "AdSlotService" as AdSlot
participant "Redis" as Redis
participant "Cosmos DB" as Cosmos

Client -> APIM: GET /api/v1/feeds/home\nAuthorization: Bearer <token>
APIM -> APIM: Validate JWT
APIM -> Controller: Request

Controller -> Controller: Extract userId from JWT
Controller -> UseCase: Get feed (userId, FEED_TYPE.HOME)

alt Cache Hit
    UseCase -> Redis: Get feed\nkey: {userId}:HOME
    Redis --> UseCase: Feed found
    
    UseCase -> AdSlot: Inject ads
    AdSlot --> UseCase: Feed with ads
    
    UseCase --> Controller: FeedDto
else Cache Miss
    UseCase -> Redis: Get feed\nkey: {userId}:HOME
    Redis --> UseCase: Not found
    
    UseCase -> VideoRepo: Get recommended videos
    VideoRepo --> UseCase: Video items
    
    UseCase -> AdSlot: Inject ads
    AdSlot --> UseCase: Feed with ads
    
    UseCase -> Cosmos: Save feed
    UseCase -> Redis: Cache feed (TTL: 30 min)
    
    UseCase --> Controller: FeedDto
end

Controller -> Controller: Generate ETag
Controller --> APIM: 200 OK\nETag: xxx
APIM --> Client: Response + ETag

opt Subsequent Request (with If-None-Match)
    Client -> APIM: GET /api/v1/feeds/home\nIf-None-Match: xxx
    
    UseCase -> Redis: Check ETag
    alt ETag Match
        APIM --> Client: 304 Not Modified
    else ETag Mismatch
        APIM --> Client: 200 OK + new ETag
    end
end

note right of Controller
  API Features:
  - ETag support (304 Not Modified)
  - Cache-Control headers
  - OAuth2 security
  - Pagination support
  - Validation
end note

@enduml
```

## Flow Description

### Cache Hit Path
1. Request arrives with OAuth2 token
2. Extract userId from JWT
3. Check Redis cache
4. Inject ads into cached feed
5. Return with ETag

### Cache Miss Path
1. Request arrives
2. Cache miss in Redis
3. Fetch videos from video repository
4. Inject ads
5. Persist to Cosmos DB
6. Cache in Redis (30 min TTL)
7. Return with ETag

### Subsequent Request
1. Client includes `If-None-Match` header
2. Compare ETags
3. Return 304 if unchanged
4. Return 200 if changed

## Performance

- **Cache Hit**: ~50ms
- **Cache Miss**: ~500ms (video fetch + ad injection)
- **Cache TTL**: 30 minutes
- **ETag Support**: Reduces bandwidth

