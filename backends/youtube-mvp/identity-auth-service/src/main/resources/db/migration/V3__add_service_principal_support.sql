-- Migration to add service principal support
-- Adds user_type and service_principal_id columns to users table

ALTER TABLE users 
  ADD COLUMN IF NOT EXISTS user_type VARCHAR(20) NOT NULL DEFAULT 'USER',
  ADD COLUMN IF NOT EXISTS service_principal_id VARCHAR(255);

-- Make email and normalized_email nullable for service principals
ALTER TABLE users 
  ALTER COLUMN email DROP NOT NULL,
  ALTER COLUMN normalized_email DROP NOT NULL;

-- Drop the old unique constraint on normalized_email (will recreate conditionally)
DROP INDEX IF EXISTS ux_users_normalized_email;

-- Create unique constraints conditionally
-- Only enforce uniqueness when the value is not null
CREATE UNIQUE INDEX IF NOT EXISTS ux_users_normalized_email 
  ON users (normalized_email) 
  WHERE normalized_email IS NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS ux_users_service_principal_id 
  ON users (service_principal_id) 
  WHERE service_principal_id IS NOT NULL;

CREATE INDEX IF NOT EXISTS ix_users_service_principal_id 
  ON users (service_principal_id) 
  WHERE service_principal_id IS NOT NULL;

-- Add check constraint for user_type
ALTER TABLE users DROP CONSTRAINT IF EXISTS ck_users_user_type;

ALTER TABLE users
  ADD CONSTRAINT ck_users_user_type
  CHECK (user_type IN ('USER', 'SERVICE_PRINCIPAL'));

-- Ensure at least one identifier is present

ALTER TABLE users DROP CONSTRAINT IF EXISTS ck_users_has_identifier;

ALTER TABLE users
  ADD CONSTRAINT ck_users_has_identifier
  CHECK (
    (user_type = 'USER' AND normalized_email IS NOT NULL) OR
    (user_type = 'SERVICE_PRINCIPAL' AND service_principal_id IS NOT NULL)
  );

