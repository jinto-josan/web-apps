# syntax=docker/dockerfile:1.7
FROM eclipse-temurin:17-jre-alpine AS runtime

WORKDIR /app

ARG JAR_FILE=target/ads-decision-service-*.jar
COPY ${JAR_FILE} app.jar

EXPOSE 8080
ENTRYPOINT ["java","-XX:+UseContainerSupport","-XX:MaxRAMPercentage=75.0","-jar","/app/app.jar"]


