-- User Profile Service Database Schema
-- Migration: V3__Create_outbox_events_table.sql

-- Ensure schema exists
CREATE SCHEMA IF NOT EXISTS user_profile;

-- Create outbox_events table for transactional outbox pattern
CREATE TABLE IF NOT EXISTS user_profile.outbox_events (
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

-- Create index for efficient querying of undelivered events
CREATE INDEX IF NOT EXISTS ix_outbox_not_dispatched 
    ON user_profile.outbox_events (created_at) 
    WHERE dispatched_at IS NULL;

-- Add comments
COMMENT ON TABLE user_profile.outbox_events IS 'Transactional outbox for reliable event publishing';
COMMENT ON COLUMN user_profile.outbox_events.id IS 'Unique identifier (ULID)';
COMMENT ON COLUMN user_profile.outbox_events.event_type IS 'Type of domain event (e.g., profile.updated)';
COMMENT ON COLUMN user_profile.outbox_events.aggregate_type IS 'Type of aggregate root (e.g., AccountProfile)';
COMMENT ON COLUMN user_profile.outbox_events.aggregate_id IS 'ID of aggregate root that generated the event';
COMMENT ON COLUMN user_profile.outbox_events.payload_json IS 'JSON payload containing event data';
COMMENT ON COLUMN user_profile.outbox_events.correlation_id IS 'Correlation ID for request tracing';
COMMENT ON COLUMN user_profile.outbox_events.causation_id IS 'Causation ID linking to causing event';
COMMENT ON COLUMN user_profile.outbox_events.traceparent IS 'W3C traceparent header for distributed tracing';
COMMENT ON COLUMN user_profile.outbox_events.created_at IS 'Timestamp when event was created';
COMMENT ON COLUMN user_profile.outbox_events.dispatched_at IS 'Timestamp when event was dispatched to broker';
COMMENT ON COLUMN user_profile.outbox_events.broker_message_id IS 'Message ID assigned by message broker';
COMMENT ON COLUMN user_profile.outbox_events.error IS 'Error message if dispatch failed';

