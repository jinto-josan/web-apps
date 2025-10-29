FROM eclipse-temurin:17-jre-alpine AS builder
WORKDIR /app
COPY . .
RUN ./mvnw -q -DskipTests package || mvn -q -DskipTests package

FROM gcr.io/distroless/java17-debian12
WORKDIR /app
COPY --from=builder /app/target/moderation-service-*.jar /app/app.jar
USER 65532:65532
EXPOSE 8080
ENTRYPOINT ["/usr/bin/java", "-jar", "/app/app.jar"]


