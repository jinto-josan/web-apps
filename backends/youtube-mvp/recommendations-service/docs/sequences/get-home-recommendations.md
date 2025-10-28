# Sequence Diagram: Get Home Recommendations

## Overview

This sequence diagram illustrates the flow of a home page recommendation request, from the REST controller through the two-stage ranking pipeline to the final response.

## PlantUML Diagram

```plantuml
@startuml
actor User
participant Controller as "RecommendationController"
participant HomeUseCase as "GetHomeRecommendationsUseCase"
participant CandidateProvider as "DefaultCandidateProvider"
participant VideoRepo as "VideoCandidateRepository"
participant RankingService as "HybridRankingService"
participant DiversityService as "CategoryDiversityService"
participant Redis as "Redis Cache"
participant Database as "PostgreSQL"

User -> Controller: GET /api/v1/recs/home?userId=123&limit=20

Controller -> Controller: Validate request parameters
Controller -> Controller: Extract JWT claims

Controller -> HomeUseCase: execute(request)

activate HomeUseCase

' Stage 1: Candidate Generation
HomeUseCase -> HomeUseCase: buildContext(request)
HomeUseCase -> CandidateProvider: getCandidates(userId, context, count=40)

activate CandidateProvider
CandidateProvider -> Redis: Check cache for userId

alt Cache Hit
    Redis --> CandidateProvider: Return cached candidates
else Cache Miss
    CandidateProvider -> VideoRepo: findCandidatesForUser(userId, limit=40)
    
    activate VideoRepo
    VideoRepo -> Database: Query videos
    Database --> VideoRepo: Return video records
    deactivate VideoRepo
    
    CandidateProvider -> Redis: Cache results
end

CandidateProvider --> HomeUseCase: List<VideoCandidate>

deactivate CandidateProvider

' Validate candidates
alt No Candidates
    HomeUseCase --> Controller: buildEmptyResponse()
    Controller --> User: Empty recommendations
else Has Candidates
    ' Stage 2: Ranking
    HomeUseCase -> RankingService: rank(candidates, userId, context)
    
    activate RankingService
    RankingService -> RankingService: For each candidate:
    loop For each VideoCandidate
        RankingService -> RankingService: calculateRecencyScore()
        RankingService -> RankingService: calculateRelevanceScore()
        RankingService -> RankingService: calculatePopularityScore()
        RankingService -> RankingService: calculateDiversityScore()
        RankingService -> RankingService: calculateHybridScore()
    end
    RankingService --> HomeUseCase: List<RecommendedItem> (sorted)
    
    deactivate RankingService
    
    ' Stage 3: Diversity
    HomeUseCase -> DiversityService: applyDiversityConstraints(items, maxPerCategory=6)
    
    activate DiversityService
    DiversityService -> DiversityService: Group by category
    DiversityService -> DiversityService: Limit items per category
    DiversityService -> DiversityService: Sort by score
    DiversityService --> HomeUseCase: List<RecommendedItem> (diversified)
    
    deactivate DiversityService
    
    ' Stage 4: Final Selection
    HomeUseCase -> HomeUseCase: limit(items, limit=20)
    HomeUseCase -> HomeUseCase: buildResponse(items, metadata)
    HomeUseCase --> Controller: RecommendationResponse
    
    deactivate HomeUseCase
    
    Controller -> Controller: Set cache-control headers
    Controller --> User: HTTP 200 OK with recommendations
end

@enduml
```

## Key Steps

### 1. Request Validation (Controller)
- Validates `userId` and `limit` parameters
- Extracts JWT claims for user context
- Builds `RecommendationRequest` DTO

### 2. Candidate Generation (Stage 1)
- Calls `CandidateProvider.getCandidates()`
- Checks Redis cache first
- On cache miss, queries `VideoCandidateRepository`
- Deduplicates candidates by videoId
- Returns 2x limit for filtering (e.g., 40 for final 20)

### 3. Multi-Factor Ranking (Stage 2)
- Calls `HybridRankingService.rank()`
- For each candidate:
  - **Recency**: Exponential decay (30%)
  - **Relevance**: Context matching (40%)
  - **Popularity**: View normalization (20%)
  - **Diversity**: Category spread (10%)
- Returns scored and sorted `RecommendedItem[]`

### 4. Diversity Filtering (Stage 3)
- Calls `CategoryDiversityService.applyDiversityConstraints()`
- Limits items per category (max 1/3 of limit)
- Preserves score-based ordering
- Returns diversified list

### 5. Final Selection (Stage 4)
- Limits to requested count (e.g., top 20)
- Builds `RecommendationResponse` with metadata:
  - Total candidates found
  - Total returned
  - Request type
  - AB test variant
  - Timestamp

### 6. Response (Controller)
- Sets HTTP headers (Cache-Control, X-Request-ID)
- Returns JSON response to client
- Logs metrics for observability

## Performance Characteristics

- **Cache Hit**: ~50ms (Redis only)
- **Cache Miss**: ~200ms (DB query + Redis write)
- **Ranking**: ~100ms for 100 candidates
- **Total Latency**: ~300ms (p95)

## Resilience Points

1. **Redis Failure**: Falls back to direct DB query
2. **DB Latency**: Circuit breaker opens after 3 failures
3. **Ranking Timeout**: Returns partial results (5s timeout)
4. **No Candidates**: Returns empty list with metadata

## Observability

- **Metrics**: Candidates count, ranking time, cache hit rate
- **Traces**: Full request lifecycle span
- **Logs**: Request ID, user ID, recommendation count
