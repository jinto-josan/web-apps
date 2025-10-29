.PHONY: help build run test clean docker-build docker-run

help:
	@echo "Available targets:"
	@echo "  build          - Build the project"
	@echo "  run            - Run the service locally"
	@echo "  test           - Run tests"
	@echo "  clean          - Clean build artifacts"
	@echo "  docker-build   - Build Docker image"
	@echo "  docker-run     - Run Docker container"
	@echo "  local-up       - Start local dependencies (Docker Compose)"
	@echo "  local-down     - Stop local dependencies"

build:
	mvn clean package -DskipTests

run:
	mvn spring-boot:run -Dspring-boot.run.profiles=local

test:
	mvn test

clean:
	mvn clean

docker-build:
	docker build -t comments-service:latest .

docker-run:
	docker run -p 8080:8080 \
		-e AZURE_TENANT_ID \
		-e AZURE_CLIENT_ID \
		-e AZURE_CLIENT_SECRET \
		-e AZURE_COSMOS_ENDPOINT \
		-e AZURE_COSMOS_KEY \
		comments-service:latest

local-up:
	docker-compose up -d

local-down:
	docker-compose down

