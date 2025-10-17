-- Notifications Service Database Schema
-- Migration: V1__Create_notifications_tables.sql

CREATE TABLE notifications (
    id VARCHAR(26) PRIMARY KEY,
    user_id VARCHAR(26) NOT NULL,
    type VARCHAR(50) NOT NULL,
    title VARCHAR(200) NOT NULL,
    body TEXT NOT NULL,
    deep_link VARCHAR(500),
    unread BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    read_at TIMESTAMP
);

CREATE TABLE device_tokens (
    id VARCHAR(26) PRIMARY KEY,
    user_id VARCHAR(26) NOT NULL,
    provider VARCHAR(10) NOT NULL,
    token VARCHAR(500) NOT NULL,
    device_id VARCHAR(255),
    app_id VARCHAR(100),
    enabled BOOLEAN NOT NULL DEFAULT TRUE,
    last_seen_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE delivery_jobs (
    id VARCHAR(26) PRIMARY KEY,
    user_id VARCHAR(26) NOT NULL,
    type VARCHAR(50) NOT NULL,
    channels TEXT[] NOT NULL,
    attempts INTEGER NOT NULL DEFAULT 0,
    scheduled_at TIMESTAMP NOT NULL,
    next_attempt_at TIMESTAMP,
    status_by_channel JSONB NOT NULL DEFAULT '{}',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notification_templates (
    id VARCHAR(26) PRIMARY KEY,
    key VARCHAR(100) NOT NULL,
    type VARCHAR(50) NOT NULL,
    channels TEXT[] NOT NULL,
    template_data JSONB NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(key, type)
);

-- Indexes for performance
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_type ON notifications(type);
CREATE INDEX idx_notifications_unread ON notifications(unread);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
CREATE INDEX idx_device_tokens_user_id ON device_tokens(user_id);
CREATE INDEX idx_device_tokens_provider ON device_tokens(provider);
CREATE INDEX idx_device_tokens_enabled ON device_tokens(enabled);
CREATE INDEX idx_device_tokens_token ON device_tokens(token);
CREATE INDEX idx_delivery_jobs_user_id ON delivery_jobs(user_id);
CREATE INDEX idx_delivery_jobs_scheduled_at ON delivery_jobs(scheduled_at);
CREATE INDEX idx_delivery_jobs_next_attempt_at ON delivery_jobs(next_attempt_at);
CREATE INDEX idx_notification_templates_key_type ON notification_templates(key, type);
