# Recommendations Service - Project Structure

## Overview

This is a production-grade recommendation microservice implementing:
- **Java 17** + **Spring Boot 3.3.x** + **Maven**
- **Hexagonal Architecture** (Domain, Application, Infrastructure layers)
- **DDD-lite** with value objects and repositories
- **CQRS** for read operations
- **Lombok** + **MapStruct** for DTOs/mappers
- **ProblemDetails (RFC7807)** error handling
- **Spring Security OAuth2** Resource Server
- **Spring Cloud Azure** integration (Cosmos, Redis, Service Bus, App Config, Key Vault)
- **Resilience4j** (circuit breaker, retry, timeout, bulkhead, rate limiter)
- **OpenTelemetry** observability

## Generated Files

### ✅ Core Application (40 Java files)

**Domain Layer** (`src/main/java/.../domain/`)
- `entities/`: RecommendedItem, UserFeatures, VideoCandidate
- `valueobjects/`: UserId, VideoId, RecommendationId, RecommendationContext, RecommendationScore, FeatureVector
- `repositories/`: UserFeaturesRepository, VideoCandidateRepository (ports)
- `services/`: CandidateProvider, RankingService, DiversityService, FeatureStore (domain services)

**Application Layer** (`src/main/java/.../application/`)
- `usecases/`: GetHomeRecommendationsUseCase, GetNextUpRecommendationsUseCase
- `dto/`: RecommendationRequest, RecommendationResponse
- `mappers/`: RecommendationMapper (MapStruct)

**Infrastructure Layer** (`src/main/java/.../infrastructure/`)
- `persistence/`: 
  - UserFeaturesRepositoryAdapter (JPA → Domain)
  - VideoCandidateRepositoryAdapter (Cosmos/Redis → Domain)
  - JPA entities for PostgreSQL
  - Cosmos entities for Azure Cosmos DB
- `services/`:
  - HybridRankingService (multi-factor scoring)
  - CategoryDiversityService (diversity constraints)
  - DefaultCandidateProvider (candidate retrieval)
  - RedisFeatureStore (feature caching)
- `config/`:
  - SecurityConfig (OAuth2)
  - OpenApiConfig (Swagger)
  - RedisConfig
  - ResilienceConfig

**Interfaces** (`src/main/java/.../interfaces/rest/`)
- `RecommendationController`: REST endpoints

**Shared** (`src/main/java/.../shared/`)
- `exceptions/`: GlobalExceptionHandler (RFC7807)
- `constants/`: ValidationConstants

### ✅ Tests (5 test files)

**Unit Tests**
- `GetHomeRecommendationsUseCaseTest`
- `RecommendationControllerTest` (WebMvcTest)

**Test Configuration**
- `TestConfiguration`
- `application-test.yml`

### ✅ Configuration (6 YAML files)

**Application Config**
- `application.yml` - Main configuration
- `application-local.yml` - Local development with emulators
- `application-dev.yml` - Development environment
- `application-test.yml` - Test configuration

**Database**
- `db/migration/V1__Initial_schema.sql` - Flyway migration

### ✅ Docker & Kubernetes

**Docker**
- `Dockerfile` - Multi-stage build (JRE Alpine)

**Kubernetes Manifests**
- `k8s/deployment.yaml` - Deployment with health probes
- `k8s/service.yaml` - ClusterIP service
- `k8s/hpa.yaml` - Horizontal Pod Autoscaler
- `k8s/pdb.yaml` - Pod Disruption Budget
- `k8s/network-policy.yaml` - Network policies

**Helm Chart**
- `charts/recommendations-service/`
  - `Chart.yaml`
  - `values.yaml`
  - `templates/deployment.yaml`
  - `templates/service.yaml`
  - `templates/hpa.yaml`
  - `templates/_helpers.tpl`

### ✅ Infrastructure

- `docker-compose.yml` - Local infrastructure (PostgreSQL, Redis)
- `Makefile` - Build/run/test commands
- `.gitignore`
- `.github/workflows/ci.yml` - CI/CD pipeline

### ✅ Documentation (4 files)

- `README.md` - Complete service documentation
- `docs/lld.md` - Low-level design with class diagrams
- `docs/sequences/get-home-recommendations.md` - PlantUML sequence diagram
- `docs/apim-policies.md` - Azure APIM policy snippets

## Endpoints

### GET /api/v1/recs/home
- Returns personalized video recommendations for home page
- Parameters: `userId`, `limit`, `device`, `location`, `language`, `abTestVariant`
- Response: List of recommendations with scores and metadata

### GET /api/v1/recs/next
- Returns next-up recommendations based on current video
- Parameters: `userId`, `videoId`, `limit`, device/location/language context
- Response: List of recommendations

## Architecture Highlights

### Two-Stage Ranking Pipeline

1. **Candidate Generation**: Multiple providers collect candidates (2x limit for filtering)
2. **Multi-Factor Ranking**: 
   - Recency: 30% (exponential decay)
   - Relevance: 40% (context matching)
   - Popularity: 20% (view normalization)
   - Diversity: 10% (category spread)
3. **Diversity Filtering**: Limit items per category (max 1/3 of limit)
4. **Final Selection**: Top-N recommendations

### Caching Strategy

- **Redis**: Feature vectors (24h TTL), candidate lists (1h TTL)
- **HTTP**: Cache-Control headers (5min for home, 2min for next-up)

### Resilience Patterns

- **Circuit Breaker**: 50% failure threshold, 30s open state
- **Retry**: 3 attempts with exponential backoff
- **Timeout**: 5s for external calls
- **Bulkhead**: 25 concurrent threads
- **Rate Limiter**: 100 requests/min per user

## Running the Service

### Local Development

```bash
# Start infrastructure
make docker-up

# Run service
make run

# Or with Maven
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

### Docker

```bash
docker build -t recommendations-service:latest .
docker run -p 8080:8080 recommendations-service:latest
```

### Kubernetes

```bash
# Deploy with k8s manifests
make deploy-k8s

# Or with Helm
helm install recommendations-service ./charts/recommendations-service
```

## Testing

```bash
# Unit tests
mvn test

# Integration tests
mvn verify -Dspring.profiles.active=test

# All checks
make check
```

## Key Technologies

- **Spring Boot 3.3.0**
- **Spring Cloud Azure 5.14.0**
- **Resilience4j 2.1.0**
- **MapStruct 1.5.5**
- **Testcontainers 1.19.3**
- **Springdoc OpenAPI 2.3.0**

## Metrics & Observability

- **Metrics**: `/actuator/prometheus`
- **Health**: `/actuator/health`
- **Traces**: OpenTelemetry with Azure Monitor
- **Logs**: Structured JSON with correlation IDs

## Compliance

- ✅ API versioning (`/api/v1/`)
- ✅ Pagination support
- ✅ Validation annotations
- ✅ ETag/If-None-Match (via Cache-Control)
- ✅ OpenAPI documentation
- ✅ ProblemDetails (RFC7807)
- ✅ Security (OAuth2 Resource Server)
- ✅ Observability (OpenTelemetry)

## Next Steps

1. Integrate real Azure ML endpoint for ranking
2. Add feature flags via Azure App Configuration
3. Implement Event Sourcing for user interactions
4. Add GraphQL endpoint for mobile clients
5. Real-time recommendations via WebSocket

