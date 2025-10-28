# Multi-stage build for production image
FROM maven:3.9-eclipse-temurin-17 AS build

WORKDIR /app

# Copy POM and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source and build
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage with distroless image
FROM gcr.io/distroless/java17-debian12:nonroot

WORKDIR /app

# Copy built JAR
COPY --from=build /app/target/drm-service-*.jar app.jar

# Run as non-root user
USER nonroot:nonroot

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "app.jar"]

