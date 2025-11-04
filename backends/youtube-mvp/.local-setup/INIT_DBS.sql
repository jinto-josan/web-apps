CREATE DATABASE identityauth;
CREATE USER identity_user WITH ENCRYPTED PASSWORD 'identity_user';
GRANT ALL PRIVILEGES ON DATABASE identityauth TO identity_user;
GRANT ALL PRIVILEGES ON DATABASE identityauth TO youtube;

-- Grant schema permissions (required for PostgreSQL 15+)
-- Connect to the database first
\c identityauth

-- Grant CREATE and USAGE privileges on public schema
GRANT CREATE ON SCHEMA public TO identity_user;
GRANT USAGE ON SCHEMA public TO identity_user;
GRANT CREATE ON SCHEMA public TO youtube;
GRANT USAGE ON SCHEMA public TO youtube;

-- Grant all privileges on all tables in public schema (for existing tables)
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO identity_user;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO youtube;

-- Grant privileges on future tables (default privileges)
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO identity_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON TABLES TO youtube;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO identity_user;
ALTER DEFAULT PRIVILEGES IN SCHEMA public GRANT ALL ON SEQUENCES TO youtube;
