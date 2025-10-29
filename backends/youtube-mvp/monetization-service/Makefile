.PHONY: help build run test clean docker-up docker-down

help: ## Show this help message
	@echo "Available targets:"
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | sort | awk 'BEGIN {FS = ":.*?## "}; {printf "\033[36m%-20s\033[0m %s\n", $$1, $$2}'

build: ## Build the application
	mvn clean package -DskipTests

run: ## Run the application locally
	mvn spring-boot:run

test: ## Run all tests
	mvn test

integration-test: ## Run integration tests
	mvn verify -Dtest=*IntegrationTest

clean: ## Clean build artifacts
	mvn clean

docker-up: ## Start Docker containers (PostgreSQL, Redis, Azurite)
	docker-compose up -d

docker-down: ## Stop Docker containers
	docker-compose down

docker-logs: ## View Docker container logs
	docker-compose logs -f

docker-build: ## Build Docker image
	docker build -t monetization-service:latest .

docker-run: docker-up ## Run in Docker
	docker run --rm -p 8080:8080 --network monetization-service_default monetization-service:latest

lint: ## Run code quality checks
	mvn checkstyle:check spotbugs:check

security-scan: ## Run security scan
	mvn org.owasp:dependency-check-maven:check

migrate: ## Run database migrations
	mvn liquibase:update

check: ## Run all checks
	mvn clean verify

