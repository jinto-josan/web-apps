up-local:
	docker compose -f ../docker-compose.yml up -d postgres redis azurite

build:
	./mvnw -q -DskipTests package

run:
	java -jar target/live-chat-service-*.jar --spring.profiles.active=local

docker:
	docker build -t live-chat-service:latest .


