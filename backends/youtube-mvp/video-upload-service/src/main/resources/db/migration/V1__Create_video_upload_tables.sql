-- Create video_upload table
CREATE TABLE IF NOT EXISTS video_upload (
    id VARCHAR(255) PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    channel_id VARCHAR(255),
    video_title VARCHAR(500),
    video_description TEXT,
    status VARCHAR(50) NOT NULL,
    total_size_bytes BIGINT NOT NULL,
    uploaded_size_bytes BIGINT DEFAULT 0,
    blob_name VARCHAR(1000),
    blob_container VARCHAR(255),
    content_type VARCHAR(100),
    etag VARCHAR(255),
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    expires_at TIMESTAMP,
    expiration_minutes INTEGER,
    error_message TEXT,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    CONSTRAINT fk_video_upload_user FOREIGN KEY (user_id) REFERENCES user_profile(id)
);

-- Create indexes for performance
CREATE INDEX IF NOT EXISTS idx_user_id ON video_upload(user_id);
CREATE INDEX IF NOT EXISTS idx_status ON video_upload(status);
CREATE INDEX IF NOT EXISTS idx_created_at ON video_upload(created_at);
CREATE INDEX IF NOT EXISTS idx_expires_at ON video_upload(expires_at);
CREATE INDEX IF NOT EXISTS idx_channel_id ON video_upload(channel_id);

-- Create upload_quota table
CREATE TABLE IF NOT EXISTS upload_quota (
    id BIGSERIAL PRIMARY KEY,
    user_id VARCHAR(255) NOT NULL,
    quota_type VARCHAR(50) NOT NULL,
    current_usage BIGINT DEFAULT 0,
    quota_limit BIGINT NOT NULL,
    period_start TIMESTAMP,
    period_end TIMESTAMP NOT NULL,
    upload_count INTEGER DEFAULT 0,
    upload_limit INTEGER DEFAULT 20,
    CONSTRAINT fk_upload_quota_user FOREIGN KEY (user_id) REFERENCES user_profile(id),
    CONSTRAINT unique_user_quota_type UNIQUE (user_id, quota_type)
);

-- Create index for quota lookups
CREATE INDEX IF NOT EXISTS idx_user_id_type ON upload_quota(user_id, quota_type);

-- Create chunk_upload table for resumable uploads
CREATE TABLE IF NOT EXISTS chunk_upload (
    id VARCHAR(255) PRIMARY KEY,
    upload_id VARCHAR(255) NOT NULL,
    chunk_number INTEGER NOT NULL,
    total_chunks INTEGER,
    chunk_size_bytes BIGINT,
    chunk_start_byte BIGINT,
    chunk_end_byte BIGINT,
    status VARCHAR(50),
    etag VARCHAR(255),
    pre_signed_url TEXT,
    expires_at TIMESTAMP,
    uploaded_at TIMESTAMP,
    error_message TEXT,
    CONSTRAINT fk_chunk_upload FOREIGN KEY (upload_id) REFERENCES video_upload(id) ON DELETE CASCADE
);

-- Create indexes for chunk lookups
CREATE INDEX IF NOT EXISTS idx_upload_id ON chunk_upload(upload_id);
CREATE INDEX IF NOT EXISTS idx_chunk_number ON chunk_upload(upload_id, chunk_number);

-- Create upload_event table for saga orchestration
CREATE TABLE IF NOT EXISTS upload_event (
    id VARCHAR(255) PRIMARY KEY,
    saga_id VARCHAR(255) NOT NULL,
    event_type VARCHAR(100) NOT NULL,
    payload JSONB,
    status VARCHAR(50) NOT NULL,
    retry_count INTEGER DEFAULT 0,
    max_retries INTEGER DEFAULT 3,
    error_message TEXT,
    created_at TIMESTAMP NOT NULL,
    processed_at TIMESTAMP
);

-- Create indexes for event processing
CREATE INDEX IF NOT EXISTS idx_saga_id ON upload_event(saga_id);
CREATE INDEX IF NOT EXISTS idx_event_status ON upload_event(status);
CREATE INDEX IF NOT EXISTS idx_event_type ON upload_event(event_type);
CREATE INDEX IF NOT EXISTS idx_created_at_event ON upload_event(created_at);

-- Add comment for documentation
COMMENT ON TABLE video_upload IS 'Stores video upload sessions with metadata and progress';
COMMENT ON TABLE upload_quota IS 'Tracks user upload quotas and limits';
COMMENT ON TABLE chunk_upload IS 'Manages chunked/resumable uploads for large files';
COMMENT ON TABLE upload_event IS 'Stores saga orchestration events';

