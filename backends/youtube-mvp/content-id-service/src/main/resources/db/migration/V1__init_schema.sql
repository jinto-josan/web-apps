-- Content ID Service Database Schema

CREATE SCHEMA IF NOT EXISTS content_id;

-- Fingerprints table
CREATE TABLE IF NOT EXISTS content_id.fingerprints (
    id UUID PRIMARY KEY,
    video_id UUID NOT NULL,
    blob_uri TEXT NOT NULL,
    algorithm VARCHAR(50) NOT NULL,
    duration_seconds INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMPTZ,
    CONSTRAINT uk_fingerprints_video UNIQUE (video_id)
);

CREATE INDEX idx_fingerprints_status ON content_id.fingerprints(status);
CREATE INDEX idx_fingerprints_created_at ON content_id.fingerprints(created_at);

-- Matches table
CREATE TABLE IF NOT EXISTS content_id.matches (
    id UUID PRIMARY KEY,
    source_fingerprint_id UUID NOT NULL,
    matched_fingerprint_id UUID NOT NULL,
    source_video_id UUID NOT NULL,
    matched_video_id UUID NOT NULL,
    score DOUBLE PRECISION NOT NULL,
    detected_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT uk_matches_fingerprints UNIQUE (source_fingerprint_id, matched_fingerprint_id)
);

CREATE INDEX idx_matches_source_video ON content_id.matches(source_video_id);
CREATE INDEX idx_matches_matched_video ON content_id.matches(matched_video_id);
CREATE INDEX idx_matches_processed ON content_id.matches(processed) WHERE processed = FALSE;

-- Claims table
CREATE TABLE IF NOT EXISTS content_id.claims (
    id UUID PRIMARY KEY,
    claimed_video_id UUID NOT NULL,
    owner_id UUID NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    dispute_status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    resolved_at TIMESTAMPTZ,
    resolution TEXT
);

CREATE TABLE IF NOT EXISTS content_id.claim_matches (
    claim_id UUID NOT NULL,
    match_id UUID NOT NULL,
    PRIMARY KEY (claim_id, match_id),
    FOREIGN KEY (claim_id) REFERENCES content_id.claims(id) ON DELETE CASCADE
);

CREATE INDEX idx_claims_claimed_video ON content_id.claims(claimed_video_id);
CREATE INDEX idx_claims_owner ON content_id.claims(owner_id);
CREATE INDEX idx_claims_status ON content_id.claims(status);

-- Outbox events table
CREATE TABLE IF NOT EXISTS content_id.outbox_events (
    id UUID PRIMARY KEY,
    aggregate_type VARCHAR(100) NOT NULL,
    aggregate_id VARCHAR(128) NOT NULL,
    event_type VARCHAR(200) NOT NULL,
    payload TEXT NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    dispatched_at TIMESTAMPTZ,
    retry_count INTEGER NOT NULL DEFAULT 0,
    broker_message_id VARCHAR(200),
    correlation_id VARCHAR(64),
    causation_id VARCHAR(64),
    traceparent VARCHAR(255)
);

CREATE INDEX idx_outbox_status ON content_id.outbox_events(status, created_at) WHERE status = 'PENDING';

-- Inbox messages for idempotency
CREATE TABLE IF NOT EXISTS content_id.inbox_messages (
    message_id VARCHAR(128) PRIMARY KEY,
    first_seen_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    processed_at TIMESTAMPTZ,
    attempts INTEGER NOT NULL DEFAULT 0,
    last_attempt_at TIMESTAMPTZ,
    error TEXT
);

