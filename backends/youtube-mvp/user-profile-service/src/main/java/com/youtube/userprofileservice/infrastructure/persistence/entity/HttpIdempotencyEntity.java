package com.youtube.userprofileservice.infrastructure.persistence.entity;

import com.youtube.common.domain.persistence.entity.HttpIdempotency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity for HTTP idempotency records.
 * Extends common-domain HttpIdempotency base class.
 */
@Entity
@Table(name = "http_idempotency", schema = "user_profile",
        uniqueConstraints = @UniqueConstraint(
            name = "ux_idempotency_key_hash",
            columnNames = {"idempotency_key", "request_hash"}
        ))
@Getter
@Setter
public class HttpIdempotencyEntity extends HttpIdempotency {
    
    // All fields are inherited from HttpIdempotency base class
    // This entity just provides the JPA mapping
}

