# Identity-Auth Service - Sequence Diagrams

This document provides comprehensive documentation for all sequence diagrams in the Identity-Auth service, detailing the authentication flows and interactions between various components.

## Overview

The Identity-Auth service implements multiple authentication flows to support different client types and security requirements. All diagrams are defined in `sequence-diagram.puml` and can be rendered using PlantUML.

## Sequence Diagrams

### 1. Exchange Entra/B2C id_token for Platform Tokens

**Purpose**: Exchange Azure AD B2C identity tokens for platform-specific JWT tokens

**Flow**:
- Client sends Azure AD B2C id_token to the platform
- Service verifies the token using OIDC verification
- User is created or updated based on token claims
- Platform issues its own JWT tokens for the user

**Key Components**:
- `AuthController`: Entry point for token exchange
- `ExchangeTokenUseCase`: Business logic for token exchange
- `OidcVerifier`: Validates Azure AD B2C tokens
- `TokenService`: Issues platform JWT tokens
- `KeyVaultSigner`: Signs tokens using Azure Key Vault

**Security Features**:
- JWT signature verification using JWKS
- Session tracking with Redis cache
- Token expiration management

### 2. Local Login

**Purpose**: Authenticate users with email/password credentials

**Flow**:
- Client provides email and password
- Service validates credentials against stored hash
- Creates session and issues JWT tokens
- Stores refresh token for future use

**Key Components**:
- `LoginUseCase`: Handles login business logic
- `PasswordHasher`: Verifies password hashes
- `SessionRepository`: Manages user sessions
- `RefreshTokenRepository`: Stores refresh tokens

**Security Features**:
- Password hashing with pepper from Key Vault
- Rate limiting protection
- Session-based authentication

### 3. Refresh Token Flow

**Purpose**: Obtain new access tokens using refresh tokens

**Flow**:
- Client sends refresh token
- Service validates token and checks for reuse
- Issues new token pair if valid
- Implements token rotation for security

**Key Components**:
- `RefreshUseCase`: Manages refresh token logic
- `RefreshTokenRepository`: Stores and validates refresh tokens
- `TokenService`: Issues new tokens

**Security Features**:
- Token rotation (new refresh token issued)
- Reuse detection and chain revocation
- Expiration validation

### 4. Device Code Flow

**Purpose**: Enable authentication on devices with limited input capabilities (e.g., Smart TVs)

**Flow**:
1. **Start**: Device requests device code and user code
2. **Activate**: User enters user code in browser to authorize device
3. **Poll**: Device polls for authorization status

**Key Components**:
- `StartDeviceFlowUseCase`: Initiates device flow
- `CompleteDeviceFlowUseCase`: Handles user authorization
- `DevicePollUseCase`: Manages polling for completion
- `Redis`: Stores device flow state

**Security Features**:
- Time-limited device codes (15 minutes)
- User authorization required
- Automatic cleanup after completion

### 5. Local Sign-Up

**Purpose**: Register new users with email verification

**Flow**:
- Client provides registration details
- Service validates input and checks for existing users
- Creates user account in PENDING_VERIFICATION state
- Sends verification email

**Key Components**:
- `SignUpUseCase`: Handles registration logic
- `CaptchaVerifier`: Prevents automated registrations
- `SignUpRateLimiter`: Rate limiting protection
- `EmailSender`: Sends verification emails

**Security Features**:
- CAPTCHA verification
- Rate limiting by email/IP
- Email verification requirement
- Password policy validation

### 6. Verify Email

**Purpose**: Complete email verification process

**Flow**:
- User clicks verification link with token
- Service validates token and activates account
- Issues authentication tokens upon successful verification

**Key Components**:
- `VerifyEmailUseCase`: Handles verification logic
- `VerificationTokenRepository`: Manages verification tokens
- `TokenService`: Issues tokens after verification

**Security Features**:
- Token-based verification
- One-time use tokens
- Automatic account activation

### 7. Resend Verification

**Purpose**: Allow users to request new verification emails

**Flow**:
- Client requests resend for pending verification
- Service validates request and rate limits
- Generates new verification token
- Sends new verification email

**Key Components**:
- `ResendVerificationUseCase`: Manages resend logic
- `SignUpRateLimiter`: Prevents abuse
- `EmailSender`: Sends new verification email

**Security Features**:
- Rate limiting protection
- Token revocation and regeneration
- Status validation

### 8. MFA Setup and Verify

**Purpose**: Enable and verify Multi-Factor Authentication using TOTP

**Flow**:
1. **Setup**: Generate TOTP secret and QR code
2. **Verify**: Validate TOTP code to enable MFA

**Key Components**:
- `SetupMfaUseCase`: Generates MFA secrets
- `VerifyMfaUseCase`: Validates MFA codes
- `TotpService`: Handles TOTP operations
- `MfaRepository`: Stores encrypted MFA secrets

**Security Features**:
- Encrypted secret storage in Key Vault
- TOTP-based authentication
- Secure secret generation

### 9. Logout

**Purpose**: Securely terminate user sessions

**Flow**:
- Client requests logout
- Service revokes session and refresh tokens
- Clears Redis cache entries

**Key Components**:
- `LogoutUseCase`: Handles logout logic
- `SessionRepository`: Revokes sessions
- `RefreshTokenRepository`: Revokes refresh tokens
- `Redis`: Clears session cache

**Security Features**:
- Complete session termination
- Refresh token chain revocation
- Cache cleanup

### 10. JWKS Endpoint

**Purpose**: Provide public keys for JWT verification

**Flow**:
- Client requests JWKS (JSON Web Key Set)
- Service retrieves public keys from Key Vault
- Returns JWKS in standard format

**Key Components**:
- `AuthController`: Exposes JWKS endpoint
- `KeyVaultSigner`: Retrieves keys from Key Vault
- `Azure Key Vault`: Stores signing keys

**Security Features**:
- Public key distribution
- Standard JWKS format
- Key rotation support

## Technical Architecture

### Key Technologies
- **Azure AD B2C**: External identity provider
- **Azure Key Vault**: Secure key storage and signing
- **Azure SQL**: Persistent data storage
- **Redis**: Caching and session management
- **JWT**: Token-based authentication
- **TOTP**: Multi-factor authentication

### Security Considerations
- All secrets encrypted in Key Vault
- Token rotation for refresh tokens
- Rate limiting on sensitive endpoints
- CAPTCHA protection for registration
- Session-based security with JTI tracking
- MFA support for enhanced security

### Error Handling
- Comprehensive error responses
- Proper HTTP status codes
- Security-focused error messages
- Audit logging for security events

## Usage

To render these diagrams:

1. Install PlantUML
2. Use the `sequence-diagram.puml` file
3. Generate images or view in PlantUML-compatible tools

```bash
# Generate PNG images
plantuml sequence-diagram.puml

# Generate SVG images
plantuml -tsvg sequence-diagram.puml
```

## Maintenance

- Update diagrams when authentication flows change
- Ensure all security considerations are documented
- Keep diagrams synchronized with implementation
- Review and update error handling scenarios
