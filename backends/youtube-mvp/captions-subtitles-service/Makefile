.PHONY: help build test clean run-local run-docker install-azure-tools

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-15s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

build: ## Build the service
	mvn clean package -DskipTests

test: ## Run tests
	mvn test

integration-test: ## Run integration tests with Testcontainers
	mvn verify

clean: ## Clean build artifacts
	mvn clean
	rm -rf target/

run-local: ## Run locally with emulators
	docker-compose -f docker-compose.yml up -d
	mvn spring-boot:run -Dspring-boot.run.profiles=local

run-docker: ## Build and run in Docker
	docker build kick docker .
	docker run -p 8080:8080 \
		-e AZURE_COSMOS_ENDPOINT=$$AZURE_COSMOS_ENDPOINT \
		-e AZURE_COSMOS_KEY=$$AZURE_COSMOS_KEY \
		-e AZURE_STORAGE_CONNECTION_STRING=$$AZURE_STORAGE_CONNECTION_STRING \
		captions-subtitles-service

stop-local: ## Stop local emulators
	docker-compose -f docker-compose.yml down

logs: ## Show service logs
	docker-compose -f docker-compose.yml logs -f captions-service

health: ## Check service health
	curl http://localhost:8080/api/v1/health

install-azure-tools: ## Install Azure emulators (MacOS)
	@echo "Installing Azure emulators..."
	brew tap azure/functions
	brew install azure-functions-core-tools@4
	@echo "Azure emulators installed. Use: docker-compose up -d for Testcontainers setup"
