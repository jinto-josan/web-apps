FROM eclipse-temurin:17-jre-alpine

LABEL maintainer="YouTube MVP Team"
LABEL description="Feeds Service - Personalized feeds with ad slots"

# Set working directory
WORKDIR /app

# Create non-root user
RUN addgroup -S spring && adduser -S spring -G spring
RUN chown -R spring:spring /app

# Copy application JAR
COPY target/feeds-service-*.jar app.jar

# Switch to non-root user
USER spring:spring

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=40s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://localhost:8080/actuator/health || exit 1

# Expose port
EXPOSE 8080

# JVM options for production
ENV JAVA_OPTS="-XX:+UseContainerSupport \
  -XX:MaxRAMPercentage=75.0 \
  -XX:+ExitOnOutOfMemoryError \
  -Djava.security.egd=file:/dev/./urandom \
  -Dspring.profiles.active=azure"

# Run application
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]

