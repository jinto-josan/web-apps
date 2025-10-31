-- Create synthetic check tables
CREATE TABLE IF NOT EXISTS synthetic_checks (
    id UUID PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    description TEXT,
    type VARCHAR(50) NOT NULL,
    endpoint VARCHAR(500) NOT NULL,
    method VARCHAR(10) NOT NULL,
    body TEXT,
    expected_status_code INTEGER NOT NULL,
    expected_body_pattern VARCHAR(500),
    timeout_seconds INTEGER,
    interval_seconds INTEGER NOT NULL,
    enabled BOOLEAN NOT NULL DEFAULT true,
    last_run_at TIMESTAMP,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL
);

CREATE TABLE IF NOT EXISTS synthetic_check_headers (
    check_id UUID NOT NULL,
    header_key VARCHAR(255) NOT NULL,
    header_value VARCHAR(500),
    PRIMARY KEY (check_id, header_key),
    FOREIGN KEY (check_id) REFERENCES synthetic_checks(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS synthetic_check_labels (
    check_id UUID NOT NULL,
    label_key VARCHAR(255) NOT NULL,
    label_value VARCHAR(500),
    PRIMARY KEY (check_id, label_key),
    FOREIGN KEY (check_id) REFERENCES synthetic_checks(id) ON DELETE CASCADE
);

-- Metadata is stored as part of the embedded result in synthetic_checks table
-- No separate metadata table needed

-- Embed synthetic_check_results in synthetic_checks table via columns
ALTER TABLE synthetic_checks ADD COLUMN IF NOT EXISTS result_executed_at TIMESTAMP;
ALTER TABLE synthetic_checks ADD COLUMN IF NOT EXISTS result_success BOOLEAN;
ALTER TABLE synthetic_checks ADD COLUMN IF NOT EXISTS result_status_code INTEGER;
ALTER TABLE synthetic_checks ADD COLUMN IF NOT EXISTS result_response_time_ms BIGINT;
ALTER TABLE synthetic_checks ADD COLUMN IF NOT EXISTS result_response_body TEXT;
ALTER TABLE synthetic_checks ADD COLUMN IF NOT EXISTS result_error_message VARCHAR(500);

CREATE INDEX IF NOT EXISTS idx_synthetic_checks_enabled ON synthetic_checks(enabled);
CREATE INDEX IF NOT EXISTS idx_synthetic_checks_last_run_at ON synthetic_checks(last_run_at);

