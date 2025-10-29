## Low-Level Design

### Modules
- domain: `NotificationPreference` aggregate, `UserNotificationPreferenceRepository` port
- application: services, DTOs, mappers, `JobPublisherPort`, `NotificationProviderPort`
- infrastructure: Cosmos repository adapter, Service Bus publisher/consumer, providers (SendGrid, Notification Hubs)

### Sequence: event -> job -> provider -> device
1. Domain event triggers job publication to Service Bus topic
2. Worker consumes job, loads user preferences
3. Chooses provider (email/push/in-app) per rules
4. Sends to device/email; records idempotency key in Redis

### Class Diagram (simplified)
```plantuml
@startuml
class NotificationPreference {
  +id: String
  +tenantId: String
  +emailEnabled: boolean
  +pushEnabled: boolean
  +inAppEnabled: boolean
}

interface UserNotificationPreferenceRepository {
  +findByUserId(tenantId, userId)
  +save(pref)
}

interface JobPublisherPort { +publishTestNotificationJob(t,u,c) }
interface NotificationProviderPort { +sendEmail(...); +sendPush(...) }

class NotificationPreferenceService
class TestNotificationService

NotificationPreferenceService --> UserNotificationPreferenceRepository
TestNotificationService --> JobPublisherPort

JobPublisherPort <|.. ServiceBusJobPublisher
NotificationProviderPort <|.. SendGridEmailProvider
NotificationProviderPort <|.. NotificationHubsPushProvider
@enduml
```


