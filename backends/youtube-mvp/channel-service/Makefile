.PHONY: help build run-local test docker-build docker-run k8s-deploy k8s-logs clean

help:
	@echo "Available targets:"
	@echo "  run-local      - Run service locally with emulators"
	@echo "  test           - Run all tests"
	@echo "  build          - Build JAR"
	@echo "  docker-build   - Build Docker image"
	@echo "  docker-run     - Run Docker container"
	@echo "  k8s-deploy     - Deploy to Kubernetes"
	@echo "  k8s-logs       - View K8s logs"
	@echo "  clean          - Clean build artifacts"

run-local:
	@echo "Starting local emulators..."
	docker-compose up -d
	@echo "Waiting for services to be ready..."
	@sleep 5
	@echo "Building and running service..."
	mvn clean package -DskipTests
	java -jar target/channel-service-*.jar --spring.profiles.active=local

test:
	mvn test

build:
	mvn clean package -DskipTests

docker-build:
	docker build -t channel-service:latest .

docker-run:
	docker run --env-file .env -p 8080:8080 channel-service:latest

k8s-deploy:
	kubectl apply -f k8s/

k8s-logs:
	kubectl logs -f deployment/channel-service

clean:
	mvn clean
	docker-compose down -v
