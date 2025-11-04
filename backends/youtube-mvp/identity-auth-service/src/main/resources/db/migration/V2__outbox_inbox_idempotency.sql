CREATE TABLE IF NOT EXISTS outbox_events (
  id                VARCHAR(26)     PRIMARY KEY,
  event_type        VARCHAR(200)   NOT NULL,
  aggregate_type    VARCHAR(100),
  aggregate_id      VARCHAR(128),
  payload_json      JSONB          NOT NULL,
  correlation_id    VARCHAR(64),
  causation_id      VARCHAR(64),
  traceparent       VARCHAR(255),
  created_at        TIMESTAMPTZ    NOT NULL DEFAULT NOW(),
  dispatched_at     TIMESTAMPTZ,
  broker_message_id VARCHAR(200),
  error             VARCHAR(4000)
);
CREATE INDEX IF NOT EXISTS ix_outbox_not_dispatched ON outbox_events (created_at) WHERE dispatched_at IS NULL;

CREATE TABLE IF NOT EXISTS inbox_messages (
  message_id      VARCHAR(128)  PRIMARY KEY,
  first_seen_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  processed_at    TIMESTAMPTZ,
  attempts        INTEGER       NOT NULL DEFAULT 0,
  last_attempt_at TIMESTAMPTZ,
  error           VARCHAR(2000)
);

-- Using surrogate primary key for JPA simplicity; unique composite for semantics
CREATE TABLE IF NOT EXISTS http_idempotency (
  id              BIGSERIAL     PRIMARY KEY,
  idempotency_key VARCHAR(128)  NOT NULL,
  request_hash    BYTEA         NOT NULL,
  response_status INTEGER,
  response_body   BYTEA,
  created_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  updated_at      TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
  CONSTRAINT ux_http_idem UNIQUE (idempotency_key, request_hash)
);
CREATE INDEX IF NOT EXISTS ix_http_idem_updated_at ON http_idempotency (updated_at);