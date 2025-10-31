CREATE TABLE purge_requests (
    id UUID PRIMARY KEY,
    resource_group VARCHAR(255) NOT NULL,
    profile_name VARCHAR(255) NOT NULL,
    content_paths TEXT NOT NULL,
    purge_type VARCHAR(50) NOT NULL,
    requested_by VARCHAR(255),
    requested_at TIMESTAMP NOT NULL,
    status VARCHAR(50) NOT NULL,
    error_message TEXT
);

CREATE INDEX idx_status ON purge_requests(status);
CREATE INDEX idx_requested_at ON purge_requests(requested_at);

