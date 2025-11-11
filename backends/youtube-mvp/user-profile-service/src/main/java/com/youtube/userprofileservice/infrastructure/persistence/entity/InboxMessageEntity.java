package com.youtube.userprofileservice.infrastructure.persistence.entity;

import com.youtube.common.domain.persistence.entity.InboxMessage;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

/**
 * Inbox message entity for idempotent event processing.
 * Extends common-domain InboxMessage with service-specific schema.
 */
@Entity
@Table(name = "inbox_messages", schema = "user_profile")
@Getter
@Setter
public class InboxMessageEntity extends InboxMessage {
    // Inherits all fields from InboxMessage base class
}


