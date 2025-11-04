# Identity Auth Service

A comprehensive authentication and authorization service for the YouTube MVP platform, supporting multiple authentication flows including local login, Azure AD B2C integration, device flow, and Multi-Factor Authentication (MFA).

## Features

- **Local Authentication** - Email/password login with secure password hashing
- **Azure AD B2C Integration** - Token exchange for external identity providers
- **Device Code Flow** - Authentication for limited-input devices (Smart TVs, etc.)
- **Multi-Factor Authentication** - TOTP-based MFA support
- **User Registration** - Email verification and account activation
- **Session Management** - Secure session handling with JWT tokens
- **Token Refresh** - Secure token rotation and reuse detection
- **Rate Limiting** - Protection against abuse and spam

## Architecture

- **Clean Architecture** - Domain-driven design with clear separation of concerns
- **Spring Boot** - Modern Java framework with auto-configuration
- **JPA/Hibernate** - Object-relational mapping for database operations
- **PostgreSQL** - Robust relational database for persistent storage
- **Redis** - In-memory cache for sessions and rate limiting
- **Flyway** - Database migration management
- **Docker** - Containerized deployment

## Quick Start

### Prerequisites

- Java 17+
- Maven 3.6+
- Docker and Docker Compose
- PostgreSQL 15+ (if running locally)
- Redis 7+ (if running locally)

### Running with Docker Compose

1. **Clone the repository and navigate to the project root:**
   ```bash
   cd /Users/jinto/Desktop/Repositories/Personal/web-apps
   ```

2. **Start all services:**
   ```bash
   docker-compose up -d
   ```

3. **Check service health:**
   ```bash
   docker-compose ps
   ```

4. **View logs:**
   ```bash
   docker-compose logs -f identity-auth-service
   ```

5. **Stop services:**
   ```bash
   docker-compose down
   ```

### Running Locally

1. **Start PostgreSQL and Redis:**
   ```bash
   # Using Docker for dependencies only
   docker-compose up -d postgres redis
   ```

2. **Build and run the application:**
   ```bash
   cd backends/youtube-mvp/identity-auth-service
   mvn clean package
   mvn spring-boot:run
   ```

## API Endpoints

### Authentication Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/login` | Local email/password login |
| POST | `/api/v1/auth/exchange` | Exchange Azure AD B2C token |
| POST | `/api/v1/auth/refresh` | Refresh access token |
| POST | `/api/v1/auth/logout` | Logout and revoke session |

### User Registration

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/signup` | Register new user |
| POST | `/api/v1/auth/signup/verify` | Verify email address |
| POST | `/api/v1/auth/signup/resend` | Resend verification email |

### Device Flow

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/device/start` | Start device code flow |
| POST | `/api/v1/auth/device/activate` | Activate device flow |
| POST | `/api/v1/auth/device/poll` | Poll for completion |

### Multi-Factor Authentication

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/api/v1/auth/mfa/setup` | Setup MFA |
| POST | `/api/v1/auth/mfa/verify` | Verify MFA code |

### Utility Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/api/v1/auth/health` | Health check |
| GET | `/.well-known/jwks.json` | JSON Web Key Set |

## Configuration

### Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `DB_USERNAME` | Database username | `identity_auth_user` |
| `DB_PASSWORD` | Database password | `identity_auth_pass` |
| `REDIS_HOST` | Redis host | `localhost` |
| `REDIS_PORT` | Redis port | `6379` |
| `JWT_ISSUER` | JWT issuer | `https://identity-auth-service.local` |
| `JWT_AUDIENCE` | JWT audience | `youtube-mvp-platform` |
| `SERVER_PORT` | Server port | `8080` |
| `LOG_LEVEL` | Logging level | `INFO` |

### Database Configuration

The service uses PostgreSQL with the following default configuration:
- **Database**: `identity_auth_db`
- **Username**: `identity_auth_user`
- **Password**: `identity_auth_pass`
- **Port**: `5432`

#### PostgreSQL 15+ Permissions

PostgreSQL 15+ has stricter default permissions on the `public` schema. The database user needs `CREATE` privilege on the `public` schema for Flyway migrations to work.

**If you encounter "permission denied for schema public" errors:**

1. **Manual Fix (Recommended):** Connect to PostgreSQL as a superuser and run:
   ```sql
   GRANT CREATE ON SCHEMA public TO identity_user;
   GRANT USAGE ON SCHEMA public TO identity_user;
   ```
   Or to grant to all users (development only):
   ```sql
   GRANT CREATE ON SCHEMA public TO PUBLIC;
   ```

2. **Using Init Script:** Copy `src/main/resources/db/init-grant-permissions.sql` and run it as a superuser before starting the application.

3. **Docker Compose:** If using Docker Compose, add an init script to grant permissions automatically.

### Redis Configuration

Redis is used for:
- Session caching
- Rate limiting
- Device flow state (optional)

Default configuration:
- **Host**: `localhost`
- **Port**: `6379`

## Database Schema

The service creates the following tables:

- `users` - User accounts and profiles
- `user_roles` - User role assignments
- `sessions` - Active user sessions
- `refresh_tokens` - Refresh token storage
- `email_verification_tokens` - Email verification tokens
- `mfa_secrets` - MFA secret storage
- `device_flows` - Device code flow state

## Security Features

- **Password Hashing** - Argon2id with pepper from Key Vault
- **Token Rotation** - Refresh tokens are rotated on each use
- **Reuse Detection** - Prevents refresh token reuse attacks
- **Rate Limiting** - Protects against brute force and spam
- **CAPTCHA Integration** - Prevents automated registrations
- **MFA Support** - TOTP-based multi-factor authentication
- **Session Management** - Secure session handling with JTI tracking

## Development

### Project Structure

```
src/main/java/com/youtube/identityauthservice/
├── application/           # Application layer (use cases)
├── domain/               # Domain layer (entities, repositories)
├── infrastructure/       # Infrastructure layer (persistence, external)
├── interfaces/          # Interface layer (REST controllers, DTOs)
└── shared/              # Shared utilities and exceptions
```

### Building

```bash
mvn clean compile
mvn test
mvn package
```

### Testing

```bash
# Run all tests
mvn test

# Run integration tests
mvn verify

# Run with coverage
mvn test jacoco:report
```

## Monitoring

The service exposes the following monitoring endpoints:

- **Health Check**: `/api/v1/auth/health`
- **Metrics**: `/actuator/metrics`
- **Prometheus**: `/actuator/prometheus`

## Troubleshooting

### Common Issues

1. **Database Connection Failed**
   - Ensure PostgreSQL is running
   - Check database credentials
   - Verify network connectivity

2. **Redis Connection Failed**
   - Ensure Redis is running
   - Check Redis configuration
   - Verify network connectivity

3. **JWT Signing Issues**
   - Check Azure Key Vault configuration
   - Verify signing key exists
   - Check Key Vault permissions

### Logs

View application logs:
```bash
# Docker Compose
docker-compose logs -f identity-auth-service

# Local
tail -f logs/identity-auth-service.log
```

## Contributing

1. Follow the existing code structure and patterns
2. Add comprehensive tests for new features
3. Update documentation for API changes
4. Follow the established naming conventions
5. Ensure all tests pass before submitting

## License

This project is licensed under the MIT License - see the LICENSE file for details.