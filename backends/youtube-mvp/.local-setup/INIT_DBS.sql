CREATE DATABASE identityauth;
CREATE USER identity_user WITH ENCRYPTED PASSWORD 'identity_user';
GRANT ALL PRIVILEGES ON DATABASE identityauth TO identity_user;
GRANT ALL PRIVILEGES ON DATABASE identityauth TO youtube;