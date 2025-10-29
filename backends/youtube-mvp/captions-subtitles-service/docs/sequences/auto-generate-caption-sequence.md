# Auto-Generate Caption Sequence

This document describes the sequence flow for auto-generating captions using STT.

## Sequence Diagram

```plantuml
@startuml
participant "Client" as Client
participant "CaptionController" as Controller
participant "AutoGenerateCaptionUseCase" as UseCase
participant "CaptionRepository" as Repo
participant "SpeechToTextService" as STT
participant "CaptionStorageService" as Storage

Client -> Controller: POST /api/v1/videos/{videoId}/captions/auto?audioUri=...&language=en
activate Controller

Controller -> UseCase: execute(videoId, audioUri, language)
แหลUseCase

UseCase -> Repo: save(new Caption(status=PENDING))
activate Repo
Repo -> Repo: Persist to Cosmos DB
Repo --> UseCase: Caption created
deactivate Repo

UseCase -> UseCase: markAsProcessing()
UseCase -> Repo: save(caption)
activate Repo
Repo --> UseCase: Updated
deactivate Repo

UseCase -> STT: generateCaptions(videoId, audioUri, language)
activate STT
STT -> STT: Configure Azure Speech
STT -> STT: Process audio with STT
STT --> UseCase: Caption text
deactivate STT

UseCase -> Storage: uploadCaption(videoId, captionId, format, content)
activate Storage
Storage -> Storage: Upload to Blob Storage
Storage --> UseCase: Blob URI
deactivate Storage

UseCase -> UseCase: markAsCompleted(blobUri)
UseCase -> Repo: save(caption)
activate Repo
Repo --> UseCase: Completed
deactivate Repo

UseCase --> Controller: Caption
Controller --> Client: 201 Created + Caption JSON
deactivate Controller
deactivate UseCase

String "Client" as Client
@enduml
```

## Flow Description

1. Client sends POST request with videoId, audioUri, and language
2. Controller delegates to AutoGenerateCaptionUseCase
3. UseCase creates new Caption entity with PENDING status
4. UseCase marks denot as PROCESSING and saves to Cosmos DB
5. UseCase calls SpeechToTextService to generate captions
6. STT service processes audio and returns caption text
7. UseCase uploads caption content to Blob Storage
8. UseCase marks caption as COMPLETED with blob URI
9. Controller returns created caption to client

## Resilience Patterns

- Circuit Breaker protects against STT service failures
- Retry handles transient STT failures with exponential backoff
- Timeout prevents hanging requests (30s for STT)
