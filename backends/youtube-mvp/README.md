# YouTube MVP - Multi-Module Maven Project

This is a comprehensive multi-module Maven project structure for a YouTube MVP (Minimum Viable Product) platform. The project follows microservices architecture principles with Spring Boot and includes all essential services needed for a video streaming platform.

## Project Structure

### Main Module
- **youtube-mvp** - Root POM with dependency management and module definitions

### Common Domain Modules
- **common-domain** - Parent module for shared domain components
  - **event-contracts** - Event contracts and DTOs for inter-service communication
  - **id-generation** - ID generation utilities and strategies
  - **error** - Common error handling and exception definitions

### Core Services
- **identity-auth-service** - Identity and authentication service
- **user-profile-service** - User profile management service
- **channel-service** - Channel management service

### Video Services
- **video-upload-service** - Video upload and processing service
- **video-transcode-service** - Video transcoding and processing service
- **video-catalog-service** - Video catalog and metadata management service

### Content & Streaming Services
- **streaming-session-service** - Video streaming session management service
- **drm-license-service** - Digital Rights Management license service
- **captions-subtitles-service** - Captions and subtitles management service
- **thumbnail-service** - Video thumbnail generation and management service
- **content-id-service** - Content ID and copyright management service
- **policy-enforcement-service** - Content policy enforcement service
- **moderation-service** - Content moderation and review service

### Search & Discovery Services
- **search-indexer-service** - Search index management and indexing service
- **search-query-service** - Search query processing and results service
- **recommendations-service** - Video recommendation engine service
- **history-service** - User watch history management service

### User Engagement Services
- **subscription-service** - Channel subscription management service
- **playlist-service** - Playlist management service
- **engagement-service** - User engagement tracking service (likes, dislikes, views)
- **comments-service** - Comments and replies management service
- **notifications-service** - User notifications management service

### Live Streaming Services
- **live-ingest-service** - Live streaming ingest and processing service
- **live-chat-service** - Live chat and real-time messaging service

### Monetization Services
- **ads-decision-service** - Advertisement decision and placement service
- **ads-tracking-service** - Advertisement tracking and analytics service
- **monetization-service** - Content monetization management service
- **billing-payout-service** - Billing and payout management service
- **studio-analytics-service** - Creator studio analytics and reporting service

### Community & Content Services
- **community-posts-service** - Community posts and updates management service
- **shorts-service** - YouTube Shorts management service
- **localization-service** - Content localization and region management service
- **translation-service** - Content translation and language processing service
- **report-abuse-service** - Content abuse reporting and moderation service

## Technology Stack

### Core Technologies
- **Java 17** - Programming language
- **Spring Boot 3.2.0** - Application framework
- **Spring Cloud 2023.0.0** - Microservices framework
- **Maven** - Build tool and dependency management

### Databases
- **PostgreSQL** - Primary relational database
- **Redis** - Caching and session management
- **MongoDB** - Document storage for specific services
- **Elasticsearch** - Search and analytics
- **InfluxDB** - Time series data for analytics

### Message Queue & Streaming
- **Apache Kafka** - Event streaming and messaging
- **WebSocket** - Real-time communication

### Cloud Services
- **Azure Blob Storage** - Object storage for videos, images, and files
- **Azure Cognitive Services** - Translation and AI services
- **Azure SDK** - Cloud service integration

### Additional Libraries
- **MapStruct** - Bean mapping
- **Lombok** - Code generation
- **Jackson** - JSON processing
- **JWT** - Authentication tokens
- **FFmpeg** - Video processing
- **Stripe** - Payment processing
- **Azure Cognitive Services** - Translation services
- **Firebase Admin** - Push notifications

### Testing
- **JUnit 5** - Unit testing
- **Testcontainers** - Integration testing with real containers
- **Mockito** - Mocking framework

## Getting Started

### Prerequisites
- Java 17 or higher
- Maven 3.6 or higher
- Docker (for Testcontainers)
- PostgreSQL
- Redis
- Elasticsearch

### Building the Project
```bash
# Build all modules
mvn clean install

# Build specific module
mvn clean install -pl <module-name>

# Skip tests
mvn clean install -DskipTests
```

### Running Services
Each service can be run independently:
```bash
cd <service-directory>
mvn spring-boot:run
```

## Architecture Principles

### Microservices Architecture
- Each service is independently deployable
- Services communicate via REST APIs and events
- Database per service pattern
- Event-driven architecture with Kafka

### Domain-Driven Design
- Common domain modules for shared concepts
- Service-specific domain models
- Event contracts for inter-service communication

### Scalability & Performance
- Redis caching for frequently accessed data
- Elasticsearch for fast search capabilities
- Time series databases for analytics
- Horizontal scaling support

### Security
- JWT-based authentication
- Service-to-service security
- Content policy enforcement
- DRM for premium content

### Monitoring & Observability
- Spring Boot Actuator for health checks
- Centralized logging
- Metrics collection
- Distributed tracing support

## Development Guidelines

### Code Organization
- Follow Spring Boot best practices
- Use MapStruct for DTO mapping
- Implement proper error handling
- Write comprehensive tests

### Database Management
- Use Flyway for database migrations
- Follow database per service pattern
- Implement proper indexing strategies

### API Design
- RESTful API design principles
- Proper HTTP status codes
- API versioning strategy
- OpenAPI documentation

### Event-Driven Communication
- Use Kafka for asynchronous communication
- Define clear event contracts
- Implement proper error handling for events
- Use event sourcing where appropriate

## Deployment

### Docker Support
The project includes Docker profiles for containerized deployment:
```bash
mvn clean package -Pdocker
```

### Environment Configuration
- Use Spring Cloud Config for centralized configuration
- Environment-specific properties
- Secrets management integration

## Contributing

1. Follow the established code style
2. Write unit and integration tests
3. Update documentation as needed
4. Follow the microservices architecture principles
5. Ensure proper error handling and logging

## License

This project is licensed under the MIT License - see the LICENSE file for details.
