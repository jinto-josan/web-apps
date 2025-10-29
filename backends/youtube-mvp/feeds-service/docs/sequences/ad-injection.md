# Ad Injection Sequence

```plantuml
@startuml
participant "GetFeedUseCase" as UseCase
participant "AdSlotService" as AdSlot
database "Ad Service" as AdService
participant "Feed" as Feed

UseCase -> AdSlot: injectAds(items, feedType)
activate AdSlot

AdSlot -> AdSlot: Calculate ad slots\n(interval: every 10 items)

loop For each ad slot
    AdSlot -> AdService: Get ad for slot
    AdService --> AdSlot: Ad details
    
    AdSlot -> AdSlot: Create FeedItem\nisAd = true
    AdSlot -> AdSlot: Insert at position
end

AdSlot -> Feed: Return items with ads
AdSlot --> UseCase: Feed with ads

note right of AdSlot
  Ad Injection Rules:
  - Every 10 items
  - Random selection
  - Balance by category
  - Skips if content < 10 items
end note

@enduml
```

## Ad Injection Algorithm

### Position Strategy

- **Interval**: Every 10 items
- **Index**: Position = 10, 20, 30, ...
- **Skip**: If feed has less than 10 items

### Ad Selection

```java
// Ad slots
- Slot 1: Position 10
- Slot 2: Position 20
- Slot 3: Position 30
```

### Example Output

```
Items 1-9: Videos
Item 10:   Ad #1 (isAd: true)
Items 11-19: Videos
Item 20:   Ad #2 (isAd: true)
...
```

## Metadata

Each ad includes:
- `isAd: true`
- `adSlotIndex: 1, 2, 3...`
- `videoId: "ad-xxx"`
- `category: "AD"`

