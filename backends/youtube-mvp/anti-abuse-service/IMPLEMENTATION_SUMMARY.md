# Anti-Abuse Service - Implementation Summary

## Overview
Production-grade microservice for real-time risk scoring and fraud detection.

## Technology Stack
- **Java 17**
- **Spring Boot 3.3.4**
- **Maven**
- **Spring Cloud Azure 5.16.0**
- **Cosmos DB** (rules, feature store)
- **Redis** (caching)
- **Azure ML** (online endpoint for predictions)
- **Event Hubs** (risk score streaming)

## Architecture
- **Hexagonal Architecture**: Domain, Application, Infrastructure layers
- **DDD-lite**: Entities, value objects, repository interfaces
- **Ports & Adapters**: Clear separation of concerns

## Key Features Implemented
1. ✅ Real-time risk scoring for events (views, ads, comments, uploads)
2. ✅ Rule engine with DSL (AND/OR conditions, multiple operators)
3. ✅ ML integration with Azure ML online endpoint
4. ✅ Feature enrichment (real-time + historical)
5. ✅ Circuit breaker for ML endpoint resilience
6. ✅ Spring Security Resource Server (OIDC)
7. ✅ OpenTelemetry observability
8. ✅ ProblemDetails error handling (RFC7807)
9. ✅ Docker & Kubernetes deployment
10. ✅ Helm charts for package management

## API Endpoints
- `POST /api/v1/risk/score` - Calculate risk score
- `POST /api/v1/rules/evaluate` - Evaluate rules

## Testing
- Unit tests for rule evaluator
- Integration tests with Testcontainers
- Jacoco code coverage

## Deployment
- Dockerfile (distroless)
- Kubernetes manifests
- Helm charts
- docker-compose.yml for local development

## Documentation
- README.md with run instructions
- Low-level design document
- Sequence diagrams for key flows

## Risk Scoring Algorithm
1. Feature enrichment from event context + feature store
2. Rule evaluation against enriched features
3. ML prediction from Azure ML endpoint
4. Score combination (70% ML, 30% rules)
5. Risk level determination (LOW/MEDIUM/HIGH/CRITICAL)
6. Enforcement action recommendation

