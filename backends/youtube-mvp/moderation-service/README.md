## Content Moderation/Policy Service

Purpose: ML moderation + human review queues; strikes; appeals.

### Stack
- Java 17, Spring Boot 3.3.x
- Hexagonal architecture (domain, application, infrastructure)
- Azure: Cosmos DB, Service Bus, App Config, Key Vault, Content Safety
- Security: OIDC Resource Server (Entra External ID/B2C)
- OpenAPI, Resilience4j, Redis idempotency (planned), OpenTelemetry

### Endpoints (v1)
- POST `/api/v1/moderation/scan`
- GET/POST `/api/v1/moderation/cases` (POST implemented)
- POST `/api/v1/moderation/appeals` (stub)

### Run Locally
```bash
make build
make run # or: ./mvnw spring-boot:run
```

Profiles: `application-local.yml` targets Azurite/Cosmos emulator.

Env vars:
- `OAUTH2_ISSUER_URI`, `AZURE_CLIENT_ID`, `AZURE_CLIENT_SECRET`, `AZURE_TENANT_ID`
- `AZURE_COSMOS_ENDPOINT`, `AZURE_COSMOS_KEY`, `AZURE_COSMOS_DB`
- `AZURE_SERVICEBUS_NAMESPACE`, `AZURE_CONTENT_SAFETY_ENDPOINT`, `AZURE_CONTENT_SAFETY_KEY`

### Docker
```bash
make docker
docker run -p 8080:8080 moderation-service:local
```

### Kubernetes (manifests)
```bash
kubectl apply -f k8s/
```

### Helm
```bash
helm upgrade --install moderation ./charts
```

### APIM Policy Snippets
JWT validation and rate limiting example:
```xml
<inbound>
  <base />
  <validate-jwt header-name="Authorization" failed-validation-httpcode="401" require-scheme="Bearer">
    <openid-config url="https://login.microsoftonline.com/{tenant}/v2.0/.well-known/openid-configuration" />
    <audiences>
      <audience>api://moderation-service</audience>
    </audiences>
  </validate-jwt>
  <rate-limit calls="60" renewal-period="60" />
</inbound>
<backend><base /></backend>
<outbound><base /></outbound>
<on-error><base /></on-error>
```

### Sequence: upload -> scan -> case -> decision
See `docs/sequences/scan-to-decision.md`.

### Reviewer API
Service Bus consumer subscribes to `moderation-review` to deliver tasks to reviewers.


