# Low-Level Design: Captions and Subtitles Service

## Overview

The Captions and Subtitles Service is responsible for managing video captions and subtitles, including auto-generation via STT, manual editing, and translations.

## Architecture

### Hexagonal Architecture

```
┌─────────────────────────────────────────────────┐
│                  Interfaces                      │
│  - REST Controllers                              │
│  - Exception Handlers                            │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│                 Application                      │
│  - Use Cases (Create, AutoGen, Translate, Get)  │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│                   Domain                         │
│  - Entities (Caption)                            │
│  - Value Objects (LanguageCode, Format, Status) │
│  - Domain Services (STT, Translation, Storage)  │
│  - Repository Interfaces                        │
└────────────────┬────────────────────────────────┘
                 │
┌────────────────▼────────────────────────────────┐
│               Infrastructure                     │
│  - Azure STT Adapter                            │
│  - Azure Translator Adapter                     │
│  - Blob Storage Adapter                         │
│  - Cosmos DB Repository Implementation          │
└─────────────────────────────────────────────────┘
```

## Core Components

### Domain Layer

#### Entity: Caption
- **Id**: Unique identifier
- **VideoId**: Reference to video
- **Language**: Language code (enum)
- **Format**: SRT/WebVTT/etc
- **Status**: PENDING/PROCESSING/COMPLETED/FAILED
- **Source**: MANUAL/AUTO/TRANSLATED
- **BlobUri**: Reference to stored caption file
- **ETag**: For version control
- **Version**: Version number for tracking changes
- **ConfidenceScore**: Quality metric for auto-generated captions
- **TranslatedFromCaptionId**: For translation tracking

#### Value Objects
- **LanguageCode**: ISO 639-1 language codes
- **CaptionFormat**: SRT, WebVTT, DFXP, SUB, ASS, VTT
- **CaptionStatus**: PENDING, PROCESSING, COMPLETED, FAILED, DELETED
- **SourceType**: MANUAL, AUTO, TRANSLATED

#### Domain Services
- **SpeechToTextService**: STT operations
- **TranslationService**: Caption translations
- **CaptionStorageService**: Blob storage operations

### Application Layer

#### Use Cases
1. **CreateCaptionUseCase**: Upload manual captions
2. **AutoGenerateCaptionUseCase**: Generate via STT
3. **TranslateCaptionUseCase**: Translate to another language
4. **GetCaptionUseCase**: Retrieve captions

### Infrastructure Layer

#### Adapters
- **AzureSpeechToTextAdapter**: Azure AI Speech integration with Resilience4j
- **AzureTranslatorAdapter**: Azure Translator integration
- **BlobCaptionStorageAdapter**: Azure Blob Storage for caption files
- **CaptionRepositoryImpl**: Cosmos DB persistence

## Data Flow

### Auto-Generation Flow
```
Client → Controller → UseCase → STT Service → UseCase → Blob Storage → UseCase → Cosmos DB → Controller → Client
```

### Translation Flow
```
Client → Controller → UseCase → Storage → UseCase → Translator → UseCase → Storage → UseCase → Cosmos DB → Controller → Client
```

## Resilience Patterns

### Circuit Breaker
- Protects against STT and Translator service failures
- Configurable thresholds and time windows
- Health indicator integration

### Retry
- Exponential backoff for transient failures
- Configurable attempts and delays
- Exception-specific retry policies

### Timeout
- Prevents hanging requests
- Configurable per service

## Security

- OAuth2 Resource Server with JWT validation
- Managed Identity for Azure services
- Network policies for Kubernetes
- ETag for optimistic concurrency

## Observability

- OpenTelemetry traces to Azure Monitor
- Prometheus metrics via Spring Actuator
- Structured logging with correlation IDs
- Health probes for Kubernetes

## Scalability

- Horizontal scaling via Kubernetes HPA
- Connection pooling for Azure services
- Cache-aside pattern with Redis
- Async processing for batch operations
