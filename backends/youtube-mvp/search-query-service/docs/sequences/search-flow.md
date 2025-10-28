# Search Flow Sequence Diagram

```plantuml
@startuml
actor User
participant "Search Controller" as Controller
participant "Application Service" as App
participant "Search Service" as Domain
participant "Azure Search" as Search

User -> Controller: GET /api/v1/search?query=java
activate Controller

Controller -> Controller: Validate request
Controller -> Controller: Check If-None-Match header

Controller -> App: search(request)
activate App

App -> App: Build SearchFilter
App -> Domain: search(query, filter, page, pageSize, sortBy)
activate Domain

Domain -> Domain: Build OData filter expression
Domain -> Search: Execute search query
activate Search

Search -> Search: Apply filters
Search -> Search: Apply pagination
Search -> Search: Sort results

Search --> Domain: SearchResults
deactivate Search

Domain --> App: SearchResult
deactivate Domain

App -> App: Map to SearchResponse
App --> Controller: SearchResponse
deactivate App

Controller -> Controller: Generate ETag
Controller --> User: 200 OK + ETag header
deactivate Controller

@enduml
```

## Description

1. **Request**: User sends search request with query and optional filters
2. **Validation**: Controller validates request parameters
3. **ETag Check**: Check If-None-Match for caching
4. **Application Service**: Orchestrates the search operation
5. **Filter Building**: Builds OData filter expression from filters
6. **Domain Service**: Executes search with Azure Search
7. **Azure Search**: Processes query with filters, pagination, sorting
8. **Response**: Returns paginated results with ETag for caching

## ETag Support

- Server generates ETag based on response content hash
- Client sends `If-None-Match` header for subsequent requests
- Server returns 304 Not Modified if content unchanged
