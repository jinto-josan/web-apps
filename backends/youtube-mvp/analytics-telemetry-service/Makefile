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
	docker build -t analytics-telemetry-service:latest .

docker-run: ## Run Docker container
	docker run -p 8080:8080 analytics-telemetry-service:latest

docker-compose-up: ## Start with Docker Compose
	docker-compose up -d

docker-compose-down: ## Stop Docker Compose
	docker-compose down

docker-compose-logs: ## View Docker Compose logs
	docker-compose logs -f analytics-telemetry-service

install-k8s: ## Install Helm chart
	helm install analytics-telemetry-service ./charts/analytics-telemetry-service \
		--namespace youtube-mvp \
		--create-namespace

uninstall-k8s: ## Uninstall Helm chart
	helm uninstall analytics-telemetry-service --namespace youtube-mvp

logs-k8s: ## View logs
	kubectl logs -f deployment/analytics-telemetry-service -n youtube-mvp

lint: ## Run code quality checks
	mvn checkstyle:check

format: ## Format code
	mvn spotless:apply

clean: ## Clean build artifacts
	mvn clean
	docker system prune -f

load-test: ## Run load test stub (requires JMH)
	mvn test-compile exec:java -Dexec.mainClass="com.youtube.analyticstelemetryservice.LoadTestStub"

.DEFAULT_GOAL := help

