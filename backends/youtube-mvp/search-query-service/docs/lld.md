# Low-Level Design: Search Query Service

## Overview

The Search Query Service implements a hexagonal architecture with domain, application, and infrastructure layers. It provides full-text search and autocomplete capabilities using Azure Cognitive Search, with eventually consistent index updates via Azure Service Bus.

## Architecture Layers

### 1. Domain Layer (`domain/`)

**Entities:**
- `SearchDocument`: Core search document entity with all searchable fields

**Value Objects:**
- `SearchFilter`: Filter criteria (category, language, duration, etc.)
- `SearchResult`: Paginated search results
- `Suggestion`: Autocomplete suggestion
- `IndexUpdateEvent`: Domain event for index updates

**Services:**
- `SearchService`: Domain service interface for search operations

### 2. Application Layer (`application/`)

**DTOs:**
- `SearchRequest`: Request with query, filters, pagination
- `SearchResponse`: Paginated search results
- `SuggestionRequest`: Autocomplete request
- `SuggestionResponse`: Suggestions response

**Services:**
- `SearchApplicationService`: Orchestrates search domain operations

**Mappers:**
- `SearchMapper`: MapStruct mapper for DTOs/entities

### 3. Infrastructure Layer (`infrastructure/`)

**Adapters:**
- `AzureSearchAdapter`: Implements `SearchService` using Azure Search
- `IndexUpdateConsumer`: Service Bus consumer for index updates

**Clients:**
- `AzureSearchClient`: Wrapper for Azure Cognitive Search SDK

**Repositories:**
- `CosmosVideoRepository`: Cosmos DB access for source data

**Configuration:**
- `SearchConfiguration`: Bean configuration
- `SecurityConfiguration`: Spring Security OAuth2

### 4. Presentation Layer (`presentation/`)

**Controllers:**
- `SearchController`: REST endpoints for search, suggest, rebuild

**Exception Handling:**
- `GlobalExceptionHandler`: RFC 7807 problem details

## Class Diagram

```
┌─────────────────────────────────────────────────────────┐
│                    SearchController                      │
│                   /api/v1/search                        │
│                   /api/v1/suggest                       │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│           SearchApplicationService                       │
│            - search()                                    │
│            - suggest()                                   │
│            - handleIndexUpdate()                         │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                  SearchService (Domain)                  │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
┌─────────────────────────────────────────────────────────┐
│                AzureSearchAdapter                        │
│      Implements SearchService using Azure Search        │
└────────────────────┬───────────────┬────────────────────┘
                     │               │
          ┌──────────┘               └──────────┐
          │                                     │
          ▼                                     ▼
┌─────────────────────┐          ┌──────────────────────┐
│ AzureSearchClient    │          │ CosmosVideoRepository│
│   - search()         │          │   - findVideoById() │
│   - suggest()        │          │   - findAllVideos()  │
│   - uploadDocuments()│          └──────────────────────┘
└─────────────────────┘

┌─────────────────────────────────────────────────────────┐
│            IndexUpdateConsumer (Service Bus)             │
│              Listens to video.* events                   │
└────────────────────┬────────────────────────────────────┘
                     │
                     ▼
            SearchApplicationService.handleIndexUpdate()
```

## Key Components

### AzureSearchClient
- Manages Azure Cognitive Search index lifecycle
- Creates index with proper field mappings and analyzers
- Handles search queries with filters and pagination
- Retry logic for 429 rate limits

### AzureSearchAdapter
- Implements `SearchService` domain interface
- Translates domain operations to Azure Search operations
- Builds OData filter expressions
- Maps documents between domains

### IndexUpdateConsumer
- Listens to Service Bus topic: `video-events`
- Subscription: `search-index-updates`
- Processes `video.published`, `video.updated`, `video.deleted` events
- Idempotent message processing

## Data Flow

### Search Flow
1. Client → Controller: GET /api/v1/search?query=java
2. Controller → ApplicationService: search()
3. ApplicationService → DomainService: search()
4. DomainService → AzureSearchAdapter: search()
5. AzureSearchClient → Azure Cognitive Search
6. Results ← Azure Cognitive Search
7. Response → Client

### Index Update Flow
1. Video Catalog Service publishes event → Service Bus
2. IndexUpdateConsumer receives event
3. Consumer → ApplicationService: handleIndexUpdate()
4. ApplicationService → DomainService: upsertDocument()
5. AzureSearchClient → Azure Search: upload document
6. Index updated

### Rebuild Flow
1. Admin → Controller: POST /api/v1/index/rebuild
2. Controller → ApplicationService: rebuildIndex()
3. ApplicationService → DomainService: rebuildIndex()
4. AzureSearchAdapter fetches all videos from Cosmos
5. Batch upload to Azure Search (100 docs/batch)

## Resilience Patterns

### Retry
- Resilience4j retry for transient failures
- Max attempts: 3
- Exponential backoff
- Retries: TimeoutException, IOException

### Circuit Breaker
- Sliding window: 10 requests
- Failure threshold: 50%
- Wait duration: 5s
- Half-open state: 3 calls

### Rate Limiting
- Handled by Azure Search client (429)
- Exponential backoff on rate limit
- Max 2 retries

## Security

- OAuth2 Resource Server (OIDC with Entra B2C)
- JWT validation at API gateway
- Role-based access: `ADMIN` for rebuild
- Service-to-service: Managed Identity

## Observability

- OpenTelemetry auto-instrumentation
- Correlation IDs in logs
- Azure Monitor integration
- Custom metrics for search performance
