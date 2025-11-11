-- User Profile Service Database Schema
-- Migration: V4__Create_inbox_messages_table.sql

-- Ensure schema exists
CREATE SCHEMA IF NOT EXISTS user_profile;

-- Create inbox_messages table for idempotent event processing (inbox pattern)
CREATE TABLE IF NOT EXISTS user_profile.inbox_messages (
    message_id VARCHAR(128) PRIMARY KEY,
    first_seen_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at TIMESTAMP,
    attempts INTEGER NOT NULL DEFAULT 0,
    last_attempt_at TIMESTAMP,
    error VARCHAR(2000)
);

-- Create indexes
CREATE INDEX IF NOT EXISTS idx_inbox_messages_processed_at ON user_profile.inbox_messages(processed_at);
CREATE INDEX IF NOT EXISTS idx_inbox_messages_first_seen_at ON user_profile.inbox_messages(first_seen_at);

-- Add comments
COMMENT ON TABLE user_profile.inbox_messages IS 'Inbox messages for idempotent event processing';
COMMENT ON COLUMN user_profile.inbox_messages.message_id IS 'Unique message identifier from the message broker (Service Bus message ID)';
COMMENT ON COLUMN user_profile.inbox_messages.processed_at IS 'Timestamp when the message was successfully processed (NULL if not yet processed)';
COMMENT ON COLUMN user_profile.inbox_messages.attempts IS 'Number of processing attempts (including retries)';
COMMENT ON COLUMN user_profile.inbox_messages.error IS 'Error message from the last failed processing attempt';

