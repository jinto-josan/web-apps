# Building Common-Domain Modules

## Quick Build

Maven will automatically resolve dependencies and build modules in the correct order. Simply run:

```bash
cd backends/youtube-mvp
mvn clean install -pl common-domain -am -DskipTests
```

## Build Order (Automatically Handled by Maven)

Maven will build modules in this order based on dependencies:

1. **common-domain-infrastructure** (no internal dependencies)
   - Contains: DomainEvent, Identifier, AggregateRoot, etc.

2. **common-domain-shared-models** (depends on infrastructure)
   - Contains: UserId, VideoId, ChannelId, Money, etc.

3. **common-domain-utilities** (no internal dependencies)
   - Contains: ValidationUtils, DateTimeUtils, etc.

4. **common-domain-error** (no internal dependencies)
   - Contains: DomainException, GlobalExceptionHandler, etc.

5. **common-domain-event-contracts** (depends on infrastructure + shared-models)
   - Contains: UserCreatedEvent, VideoPublishedEvent, ChannelCreatedEvent

## Artifact IDs Reference

| Module | Artifact ID |
|--------|-------------|
| Infrastructure | `common-domain-infrastructure` |
| Shared Models | `common-domain-shared-models` |
| Utilities | `common-domain-utilities` |
| Error Handling | `common-domain-error` |
| Event Contracts | `common-domain-event-contracts` |

**Note**: All modules now use the consistent `common-domain-<module>` naming pattern

## After Building

Once built, all services can reference these modules. The compilation errors in IDEs will resolve once the modules are installed to the local Maven repository (`~/.m2/repository`).

## Verifying Build

After building, verify with:

```bash
mvn dependency:tree -pl common-domain/event-contracts
```

This should show all dependencies resolved correctly.

