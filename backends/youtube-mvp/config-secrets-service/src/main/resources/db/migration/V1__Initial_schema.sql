-- Configuration entries table
CREATE TABLE configuration_entries (
    id VARCHAR(255) PRIMARY KEY,
    scope VARCHAR(255) NOT NULL,
    key VARCHAR(255) NOT NULL,
    value TEXT,
    content_type VARCHAR(100),
    label VARCHAR(255),
    etag VARCHAR(255),
    is_secret BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by VARCHAR(255),
    updated_by VARCHAR(255),
    CONSTRAINT uk_scope_key UNIQUE (scope, key)
);

CREATE INDEX idx_scope_key ON configuration_entries(scope, key);

-- Audit logs table
CREATE TABLE audit_logs (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    scope VARCHAR(255) NOT NULL,
    key VARCHAR(255) NOT NULL,
    action VARCHAR(100) NOT NULL,
    user_id VARCHAR(255),
    tenant_id VARCHAR(255),
    timestamp TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    details TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT
);

CREATE INDEX idx_audit_scope_key ON audit_logs(scope, key);
CREATE INDEX idx_audit_user_id ON audit_logs(user_id);
CREATE INDEX idx_audit_tenant_id ON audit_logs(tenant_id);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);

