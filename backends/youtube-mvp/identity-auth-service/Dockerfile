# Multi-stage build for Identity Auth Service
FROM maven:3.9.4-openjdk-17-slim AS build

# Set working directory
WORKDIR /app

# Copy pom.xml first for better layer caching
COPY backends/youtube-mvp/identity-auth-service/pom.xml .

# Download dependencies
RUN mvn dependency:go-offline -B

# Copy source code
COPY backends/youtube-mvp/identity-auth-service/src ./src

# Build the application
RUN mvn clean package -DskipTests

# Runtime stage
FROM openjdk:17-jre-slim

# Install curl for health checks
RUN apt-get update && apt-get install -y curl && rm -rf /var/lib/apt/lists/*

# Create app user
RUN groupadd -r appuser && useradd -r -g appuser appuser

# Set working directory
WORKDIR /app

# Copy the JAR file from build stage
COPY --from=build /app/target/identity-auth-service-*.jar app.jar

# Change ownership to app user
RUN chown appuser:appuser app.jar

# Switch to app user
USER appuser

# Expose port
EXPOSE 8080

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
  CMD curl -f http://localhost:8080/api/v1/auth/health || exit 1

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]
