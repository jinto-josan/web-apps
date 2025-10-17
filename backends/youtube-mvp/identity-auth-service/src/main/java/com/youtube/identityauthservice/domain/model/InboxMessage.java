package com.youtube.identityauthservice.domain.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@Entity
@Table(name = "inbox_messages", schema = "auth")
@Getter
@Setter
public class InboxMessage {

    @Id
    @Column(name = "message_id", length = 128)
    private String messageId;

    @Column(name = "first_seen_at", nullable = false)
    private Instant firstSeenAt = Instant.now();

    @Column(name = "processed_at")
    private Instant processedAt;

    @Column(name = "attempts", nullable = false)
    private int attempts = 0;

    @Column(name = "last_attempt_at")
    private Instant lastAttemptAt;

    @Column(name = "error", length = 2000)
    private String error;
}