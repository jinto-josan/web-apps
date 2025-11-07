-- User Profile Service Database Schema
-- Migration: V2__Create_idempotency_table.sql

-- Create http_idempotency table for HTTP idempotency pattern
CREATE TABLE IF NOT EXISTS http_idempotency (
    id BIGSERIAL PRIMARY KEY,
    idempotency_key VARCHAR(128) NOT NULL,
    request_hash BYTEA NOT NULL,
    response_status INTEGER,
    response_body BYTEA,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    CONSTRAINT ux_idempotency_key_hash UNIQUE (idempotency_key, request_hash)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_http_idempotency_key ON http_idempotency(idempotency_key);
CREATE INDEX IF NOT EXISTS idx_http_idempotency_created_at ON http_idempotency(created_at);

-- Add comments
COMMENT ON TABLE http_idempotency IS 'HTTP idempotency records for safe request retries';
COMMENT ON COLUMN http_idempotency.idempotency_key IS 'Idempotency key from Idempotency-Key header';
COMMENT ON COLUMN http_idempotency.request_hash IS 'SHA-256 hash of request signature (method + URI + body)';
COMMENT ON COLUMN http_idempotency.response_status IS 'HTTP status code of cached response';
COMMENT ON COLUMN http_idempotency.response_body IS 'Cached response body bytes';

