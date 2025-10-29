FROM eclipse-temurin:17-jre-alpine as runtime
WORKDIR /app
ARG JAR_FILE=target/live-chat-service-*.jar
COPY ${JAR_FILE} app.jar
EXPOSE 8080
ENTRYPOINT ["java","-XX:MaxRAMPercentage=75.0","-Djava.security.egd=file:/dev/./urandom","-jar","/app/app.jar"]


