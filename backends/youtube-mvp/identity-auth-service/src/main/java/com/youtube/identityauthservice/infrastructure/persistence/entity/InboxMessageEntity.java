package com.youtube.identityauthservice.infrastructure.persistence.entity;

import com.youtube.common.domain.persistence.entity.InboxMessage;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity for InboxMessage in identity-auth-service.
 * Extends common-domain InboxMessage with service-specific table configuration.
 */
@Entity
@Table(name = "inbox_messages")
@Getter
@Setter
public class InboxMessageEntity extends InboxMessage {
    // Table configuration is inherited from @Table annotation
    // All fields are inherited from InboxMessage @MappedSuperclass
}

