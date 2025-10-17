# Identity Auth Service - Sequence Diagrams

This directory contains PlantUML sequence diagrams documenting the authentication flows in the Identity Auth Service.

## 📊 Available Diagrams

### 1. Exchange AAD Token Flow
**File**: `exchange-token-sequence.puml`  
**Description**: Shows how Azure AD ID tokens are exchanged for platform-specific access and refresh tokens.

**Key Components**:
- Azure AD OIDC verification
- User lookup/linking by AAD subject or email
- Session creation
- JWT token generation with Key Vault signing
- Refresh token storage

### 2. Local Login Flow
**File**: `login-sequence.puml`  
**Description**: Traditional email/password authentication flow.

**Key Components**:
- Email/password validation
- Password hashing verification with Key Vault pepper
- Session creation
- Token generation

### 3. Refresh Token Flow
**File**: `refresh-token-sequence.puml`  
**Description**: Token refresh with rotation and reuse detection.

**Key Components**:
- Refresh token validation
- Token rotation (new refresh token replaces old)
- Reuse detection and chain revocation
- New access token generation

### 4. Device Code Flow
**File**: `device-flow-sequence.puml`  
**Description**: OAuth 2.0 Device Authorization Grant flow for TV/limited input devices.

**Key Components**:
- Device code generation and storage in Redis
- User authorization via browser
- Polling mechanism for token retrieval
- Cleanup of device codes

### 5. MFA Setup Flow
**File**: `mfa-setup-sequence.puml`  
**Description**: Multi-factor authentication setup using TOTP.

**Key Components**:
- Secret generation and encryption
- QR code/URI generation
- Secure storage in Key Vault

### 6. MFA Verification Flow
**File**: `mfa-verify-sequence.puml`  
**Description**: MFA code verification and user enablement.

**Key Components**:
- TOTP code validation
- Secret decryption from Key Vault
- User MFA status update

### 7. Logout Flow
**File**: `logout-sequence.puml`  
**Description**: User logout and session cleanup.

**Key Components**:
- Session revocation
- Refresh token chain revocation
- Redis session cleanup

### 8. JWKS Endpoint
**File**: `jwks-sequence.puml`  
**Description**: JSON Web Key Set endpoint for JWT verification.

**Key Components**:
- Public key retrieval from Key Vault
- JWKS format generation
- Public endpoint for token verification

## 🛠️ Generating Diagrams

### Prerequisites
- Java 17 or higher
- PlantUML JAR file
- Graphviz (for rendering)

### Commands
```bash
# Generate all diagrams
java -jar plantuml.jar -tpng *.puml

# Generate specific diagram
java -jar plantuml.jar -tpng exchange-token-sequence.puml

# Generate SVG format
java -jar plantuml.jar -tsvg *.puml

# Validate syntax only
java -jar plantuml.jar -checkonly *.puml
```

## 📋 Diagram Features

- **Font Consistency**: All diagrams use Helvetica font
- **Auto-numbering**: Sequence steps are automatically numbered
- **Clear Participants**: External services, repositories, and use cases clearly labeled
- **Error Handling**: Shows both success and error flows
- **Security Focus**: Highlights security-critical operations like token signing and secret management

## 🔐 Security Considerations

The diagrams highlight several security features:

1. **Token Rotation**: Refresh tokens are rotated on each use
2. **Reuse Detection**: Token reuse triggers chain revocation
3. **Secure Storage**: Secrets encrypted in Azure Key Vault
4. **Session Management**: Proper session lifecycle management
5. **MFA Integration**: Multi-factor authentication flows
6. **OIDC Compliance**: Standards-compliant OAuth/OIDC flows

## 📚 Related Documentation

- **LLD Diagram**: `lld.puml` - Low-level design class diagram
- **Database Schema**: `src/main/resources/db/migration/` - Flyway migrations
- **API Documentation**: REST API specifications
- **Architecture**: Clean Architecture folder structure

## 🔄 Updates

When updating these diagrams:

1. **Maintain Consistency**: Keep participant names consistent across diagrams
2. **Update Numbers**: Ensure step numbering reflects current implementation
3. **Security Review**: Verify security flows are accurately represented
4. **Test Generation**: Always test diagram generation after changes
5. **Documentation**: Update this README if adding new diagrams

These sequence diagrams provide a comprehensive view of the authentication flows and serve as living documentation for the Identity Auth Service implementation.
