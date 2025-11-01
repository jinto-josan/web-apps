package com.youtube.identityauthservice.infrastructure.persistence.entity;

import com.youtube.common.domain.persistence.entity.HttpIdempotency;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

/**
 * JPA entity for HttpIdempotency in identity-auth-service.
 * Extends common-domain HttpIdempotency with service-specific table configuration.
 */
@Entity
@Table(name = "http_idempotency", schema = "auth",
        uniqueConstraints = {
                @UniqueConstraint(name = "ux_auth_http_idem", columnNames = {"idempotency_key", "request_hash"})
        }
)
@Getter
@Setter
public class HttpIdempotencyEntity extends HttpIdempotency {
    // Table configuration is inherited from @Table annotation
    // All fields are inherited from HttpIdempotency @MappedSuperclass
}

