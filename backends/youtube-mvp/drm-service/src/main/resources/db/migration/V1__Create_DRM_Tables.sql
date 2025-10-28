-- Create DRM policies table
CREATE TABLE drm_policies (
    id VARCHAR(26) PRIMARY KEY,
    video_id VARCHAR(255) NOT NULL UNIQUE,
    provider VARCHAR(50) NOT NULL,
    configuration JSONB NOT NULL,
    rotation_policy JSONB,
    created_at TIMESTAMP NOT NULL,
    updated_at TIMESTAMP NOT NULL,
    created_by VARCHAR(255) NOT NULL,
    updated_by VARCHAR(255) NOT NULL,
    version BIGINT NOT NULL,
    CONSTRAINT idx_video_id UNIQUE (video_id)
);

CREATE INDEX idx_drm_policies_video_id ON drm_policies(video_id);

-- Create policy license configuration table
CREATE TABLE policy_license_config (
    policy_id VARCHAR(26) NOT NULL,
    config_key VARCHAR(255) NOT NULL,
    config_value TEXT,
    PRIMARY KEY (policy_id, config_key),
    FOREIGN KEY (policy_id) REFERENCES drm_policies(id) ON DELETE CASCADE
);

-- Create policy allowed applications table
CREATE TABLE policy_allowed_apps (
    policy_id VARCHAR(26) NOT NULL,
    application VARCHAR(255) NOT NULL,
    PRIMARY KEY (policy_id, application),
    FOREIGN KEY (policy_id) REFERENCES drm_policies(id) ON DELETE CASCADE
);

-- Create audit logs table
CREATE TABLE audit_logs (
    id VARCHAR(26) PRIMARY KEY,
    policy_id VARCHAR(26) NOT NULL,
    action VARCHAR(50) NOT NULL,
    changed_by VARCHAR(255) NOT NULL,
    changed_at TIMESTAMP NOT NULL,
    correlation_id VARCHAR(255),
    FOREIGN KEY (policy_id) REFERENCES drm_policies(id) ON DELETE CASCADE
);

CREATE INDEX idx_audit_logs_policy_id ON audit_logs(policy_id);
CREATE INDEX idx_audit_logs_changed_at ON audit_logs(changed_at);

-- Create audit old values table
CREATE TABLE audit_old_values (
    audit_id VARCHAR(26) NOT NULL,
    field_name VARCHAR(255) NOT NULL,
    field_value TEXT,
    PRIMARY KEY (audit_id, field_name),
    FOREIGN KEY (audit_id) REFERENCES audit_logs(id) ON DELETE CASCADE
);

-- Create audit new values table
CREATE TABLE audit_new_values (
    audit_id VARCHAR(26) NOT NULL,
    field_name VARCHAR(255) NOT NULL,
    field_value TEXT,
    PRIMARY KEY (audit_id, field_name),
    FOREIGN KEY (audit_id) REFERENCES audit_logs(id) ON DELETE CASCADE
);

