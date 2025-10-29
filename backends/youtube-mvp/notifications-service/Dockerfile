FROM gcr.io/distroless/java17-debian12

WORKDIR /app
COPY target/notifications-service-0.0.1-SNAPSHOT.jar app.jar

EXPOSE 8080
USER 65532:65532
ENTRYPOINT ["/usr/bin/java", "-jar", "/app/app.jar"]


