# Live Chat Service - Low Level Design

- Hexagonal architecture: domain, application, infrastructure, interfaces.
- Ports/Adapters: `ChatMessageRepository` (port) with Cosmos adapter; WebPubSub adapter; Service Bus adapter.
- CQRS-lite: read history from Redis/Cosmos, write via REST -> Cosmos + broadcast.

## Core classes
- `ChatMessage` entity; `LiveId` value object
- `ChatService` application service
- `ChatController` REST

## Data flow
1. Client requests token -> WebPubSub token issued
2. Client sends message -> idempotency check -> persist (Cosmos) -> cache recent (Redis) -> publish moderation event -> broadcast via WebPubSub
