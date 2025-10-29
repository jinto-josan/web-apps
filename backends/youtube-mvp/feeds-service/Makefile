.PHONY: help build test run docker-build docker-run clean

help: ## Show this help message
	@echo "Usage: make [target]"
	@echo ""
	@echo "Targets:"
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-15s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

build: ## Build the application
	mvn clean package -DskipTests

test: ## Run tests
	mvn test

test-integration: ## Run integration tests
	mvn verify -P integration-tests

coverage: ## Generate test coverage report
	mvn test jacoco:report
	@echo "Coverage report: target/site/jacoco/index.html"

run-local: ## Run locally with emulators
	mvn spring-boot:run -Dspring-boot.run.profiles=local

docker-build: ## Build Docker image
	docker build -t feeds-service:latest .

docker-run: ## Run Docker container
	docker run -p 8080:8080 feeds-service:latest

docker-compose-up: ## Start with Docker Compose
	docker-compose up -d

docker-compose-down: ## Stop Docker Compose
	docker-compose down

install-k8s: ## Install Helm chart
	helm install feeds-service ./charts/feeds-service \
		--namespace youtube-mvp \
		--create-namespace

uninstall-k8s: ## Uninstall Helm chart
	helm uninstall feeds-service --namespace youtube-mvp

logs-k8s: ## View logs
	kubectl logs -f deployment/feeds-service -n youtube-mvp

lint: ## Run code quality checks
	mvn checkstyle:check

format: ## Format code
	mvn spotless:apply

clean: ## Clean build artifacts
	mvn clean
	docker system prune -f

.DEFAULT_GOAL := help

