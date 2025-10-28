# Search Query Service - Setup Guide

## Quick Start

### Prerequisites
- JDK 17+
- Maven 3.9+
- Docker & Docker Compose
- Azure CLI (for cloud deployment)

### 1. Local Development with Emulators

```bash
# Start emulators (Cosmos, Service Bus)
make local-start

# Run the service
make run-local

# Test the API
curl http://localhost:8080/api/v1/search?query=test
```

### 2. Build and Test

```bash
# Build
make build

# Run tests
make test

# Build Docker image
make docker-build
```

### 3. Cloud Deployment

#### Azure Prerequisites
1. Create Azure Cognitive Search service
2. Create Cosmos DB account and database
3. Create Service Bus namespace, topic, and subscription
4. Configure App Configuration and Key Vault

#### Environment Variables
```bash
export AZURE_COSMOS_ENDPOINT="https://..."
export AZURE_COSMOS_KEY="..."
export AZURE_SEARCH_ENDPOINT="https://..."
export AZURE_SEARCH_API_KEY="..."
export AZURE_SERVICEBUS_CONNECTION_STRING="..."
```

#### Deploy to Kubernetes
```bash
# Install Helm chart
helm install search-query-service ./charts/search-query-service \
  --set azure.cosmos.endpoint=$AZURE_COSMOS_ENDPOINT \
  --set azure.search.endpoint=$AZURE_SEARCH_ENDPOINT

# Or use direct K8s manifests
kubectl apply -f k8s/
```

## API Examples

### Search
```bash
curl -X GET "http://localhost:8080/api/v1/search?query=java+tutorial&category=programming&page=1&pageSize=20" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Autocomplete
```bash
curl -X GET "http://localhost:8080/api/v1/suggest?prefix=java&maxResults=10" \
  -H "Authorization: Bearer $JWT_TOKEN"
```

### Rebuild Index (Admin)
```bash
curl -X POST "http://localhost:8080/api/v1/index/rebuild" \
  -H "Authorization: Bearer $ADMIN_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"forceRebuild":true}'
```

## Monitoring

- Health: http://localhost:8080/actuator/health
- Swagger UI: http://localhost:8080/swagger-ui.html
- Prometheus: http://localhost:8080/actuator/prometheus

## Troubleshooting

### Service Bus consumer not receiving messages
- Check subscription name: `search-index-updates`
- Verify topic: `video-events`
- Check dead-letter queue

### Search index not updating
- Check consumer logs
- Verify message format from video catalog service
- Check Cosmos DB for source data

### Azure Search errors
- Check index exists: `video-search-index`
- Verify API key has read/write permissions
- Check rate limits (429 errors)

## Next Steps

1. Configure production secrets in Key Vault
2. Set up Azure Monitor alerts
3. Configure autoscaling (HPA)
4. Set up CI/CD pipeline
5. Add integration tests with Testcontainers
