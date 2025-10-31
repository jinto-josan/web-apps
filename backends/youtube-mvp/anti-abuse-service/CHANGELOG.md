# Changelog

All notable changes to the Anti-Abuse Service will be documented in this file.

## [0.1.0] - 2024-01-XX

### Added
- Initial release
- Real-time risk scoring for events (views, ads, comments, uploads)
- Rule engine with DSL (AND/OR conditions, multiple operators)
- ML integration with Azure ML online endpoint
- Feature enrichment (real-time + historical)
- Circuit breaker for ML endpoint resilience
- Spring Security Resource Server (OIDC) integration
- OpenTelemetry observability
- Resilience4j patterns (circuit breaker, retry, timeout)
- ProblemDetails error handling (RFC7807)
- Validation exception handlers
- Custom health indicators
- Docker and Kubernetes deployment artifacts
- Helm charts
- Comprehensive test coverage

### Infrastructure
- Docker Compose for local development
- Kubernetes manifests (deployment, service, HPA, network policy, PDB)
- Helm charts with templates
- Bootstrap configuration for Key Vault

