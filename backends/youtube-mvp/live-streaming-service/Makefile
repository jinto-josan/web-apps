.PHONY: help build test clean run-local docker-build docker-up docker-down k8s-deploy logs

help:
	@echo "Available targets:"
	@echo "  make build           - Build the application"
	@echo "  make test            - Run tests"
	@echo "  make clean           - Clean build artifacts"
	@echo "  make run-local       - Run locally with emulators"
	@echo "  make docker-build    - Build Docker image"
	@echo "  make docker-up       - Start dependencies with Docker Compose"
	@echo "  make docker-down     - Stop Docker Compose services"
	@echo "  make k8s-deploy      - Deploy to Kubernetes"
	@echo "  make logs             - View application logs"

build:
	mvn clean package -DskipTests

test:
	mvn test

clean:
	mvn clean
	rm -rf target

run-local:
	@echo "Starting local environment..."
	docker-compose up -d
	@echo "Waiting for services to be ready..."
	sleep 10
	SPRING_PROFILES_ACTIVE=local mvn spring-boot:run

docker-build:
	docker build -t live-streaming-service:latest .

docker-up:
	docker-compose up -d

docker-down:
	docker-compose down -v

k8s-deploy:
	kubectl apply -f k8s/

logs:
	kubectl logs -f deployment/live-streaming-service

