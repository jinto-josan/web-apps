# Streaming Session Service

A production-grade microservice for video playback session management with CDN integration, DRM support, and ABAC policy engine.

## Features

- **Session Management**: Redis-backed playback sessions
- **CDN Integration**: Azure Front Door + Blob Storage
- **DRM Support**: Widevine, PlayReady, FairPlay
- **Policy Engine**: ABAC-based geo/device restrictions
- **Signed URLs**: JWT tokens for secure access
- **Manifest Generation**: HLS/DASH manifest URLs
- **GeoIP Detection**: Country/region validation
- **Rate Limiting**: Per-user and per-IP limits
- **Cache Headers**: Efficient CDN caching
- **Observability**: OpenTelemetry + correlation IDs

## Architecture

```
┌─────────────────────────────────────────────────────────────┐
│                   Presentation Layer                         │
│  PlaybackController (Manifest, Token endpoints)             │
│              Cache headers, Rate limiting                    │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                   Application Layer                          │
│  ┌──────────────────────┐  ┌──────────────────────┐       │
│  │ PlaybackService       │  │ TokenService         │       │
│  │ - Start session       │  │ - Generate JWT       │       │
│  │ - Get manifest        │  │ - Validate token     │       │
│  │ - Policy checks      │  │ - Sign URLs          │       │
│  └──────────────────────┘  └──────────────────────┘       │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                     Domain Layer                             │
│  PlaybackSession | DeviceInfo | PolicyEngine                 │
│  GeoIP checks | Device compatibility | DRM checks           │
└────────────────────┬─────────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────────┐
│                  Infrastructure Layer                         │
│  Redis (sessions) | Cosmos (metadata) | CDN | DRM          │
└─────────────────────────────────────────────────────────────┘
```

## Tech Stack

- **Java 17**
- **Spring Boot 3.3.4**
- **Redis**: Session storage
- **Azure Cosmos DB**: Video metadata
- **Azure Front Door**: CDN
- **Azure Blob Storage**: Video content origin
- **JWT**: Signed URL tokens
- **MaxMind GeoIP2**: Location detection
- **Spring Cloud Azure**: 5.15.0

## API Endpoints

### Get Manifest URL
```bash
GET /api/v1/playback/{videoId}/manifest
Authorization: Bearer <token>

Response:
{
  "manifestUrl": "https://cdn.example.com/videos/123/master.m3u8",
  "token": "eyJhbGc...",
  "expiresAt": "2024-01-02T12:00:00Z"
}
```

### Get Playback Token
```bash
GET /api/v1/playback/{videoId}/token
Authorization: Bearer <token>

Response:
{
  "token": "eyJhbGc...",
  "expiresAt": "2024-01-02T12:00:00Z"
}
```

## Local Development

### Prerequisites
- Java 17
- Maven 3.9+
- Docker (for Redis)

### Using Redis Docker
```bash
docker run -d -p 6379:6379 redis:7-alpine
```

### Run Service
```bash
mvn spring-boot:run -Dspring-boot.run.profiles=local
```

## Configuration

### Environment Variables
```bash
AZURE_REDIS_HOST=localhost
AZURE_REDIS_PORT=6379
AZURE_COSMOS_ENDPOINT=https://...
AZURE_COSMOS_KEY=...
AZURE_STORAGE_CONNECTION_STRING=...
AZURE_STORAGE_CONTAINER=videos
CDN_BASE_URL=https://cdn.example.com
JWT_SECRET=your-secret-key
JWT_ISSUER=streaming-service
```

## Policy Engine (ABAC)

The service implements Attribute-Based Access Control:

- **Geo Restrictions**: Check allowed/blocked regions
- **Device Compatibility**: Device type, browser, OS checks
- **Time Restrictions**: Time-based viewing windows
- **DRM Requirements**: Encryption/decryption capabilities
- **IP Reputation**: Block suspicious IPs
- **Subscription Tiers**: Free vs. Premium content

## CDN Integration

### Manifest URLs
- **HLS**: `/videos/{videoId}/master.m3u8`
- **DASH**: `/videos/{videoId}/manifest.mpd`

### Signed URLs
- JWT-based tokens
- Expiration (typically 1 hour)
- Device binding
- CDN caching (24 hours)

## Security

### Token Structure
```json
{
  "sub": "user-123",
  "videoId": "video-456",
  "exp": 1704204000,
  "ip": "192.168.1.1",
  "deviceId": "device-789",
  "iat": 1704200400
}
```

### Validation Checks
1. Token signature
2. Expiration
3. IP address (anti-token theft)
4. Device ID binding
5. Video ownership/access

## Observability

### Metrics
- `playback.session.create`
- `playback.session.end`
- `playback.manifest.request`
- `playback.token.generate`
- `playback.bytes.delivered`
- `policy.check.allowed/denied`

### Traces
- OpenTelemetry instrumentation
- Correlation IDs in headers
- Span context propagation

### Logs
- Structured JSON logging
- Geo information
- Device details
- Policy decisions

## Testing

### Unit Tests
```bash
mvn test
```

### Integration Tests
```bash
mvn verify
```

Uses Testcontainers for Redis and Cosmos emulator.

## Deployment

### Docker
```bash
docker build -t streaming-session-service:latest .
docker run -p 8080:8080 streaming-session-service:latest
```

### Kubernetes
```bash
kubectl apply -f k8s/deployment.yaml
kubectl apply -f k8s/secrets.yaml
```

## Performance

- **P50**: < 50ms
- **P95**: < 200ms
- **P99**: < 500ms
- **Throughput**: 5000 req/s per instance

## Next Steps

1. Add DRM key rotation
2. Implement session QoS tracking
3. Add A/B testing support
4. Implement playback analytics
5. Add ad insertion

## License

MIT

