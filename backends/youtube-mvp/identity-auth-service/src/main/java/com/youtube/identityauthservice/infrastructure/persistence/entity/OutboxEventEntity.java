package com.youtube.identityauthservice.infrastructure.persistence.entity;

import com.youtube.common.domain.persistence.entity.OutboxEvent;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity for OutboxEvent in identity-auth-service.
 * Extends common-domain OutboxEvent with service-specific table configuration.
 */
@Entity
@Table(name = "outbox_events",
        indexes = { @Index(name = "ix_outbox_not_dispatched", columnList = "created_at") }
)
@Getter
@Setter
public class OutboxEventEntity extends OutboxEvent {
    // Table configuration is inherited from @Table annotation
    // All fields are inherited from OutboxEvent @MappedSuperclass
}

