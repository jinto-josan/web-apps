.PHONY: help build test run docker-build docker-up docker-down clean

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-15s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

build: ## Build the project
	mvn clean package -DskipTests

test: ## Run tests
	mvn clean test

test-integration: ## Run integration tests
	mvn verify -Dspring.profiles.active=test

run: ## Run the application locally
	mvn spring-boot:run -Dspring-boot.run.profiles=local

docker-build: ## Build Docker image
	docker build -t recommendations-service:latest .

docker-up: ## Start infrastructure with Docker Compose
	docker-compose up -d

docker-down: ## Stop infrastructure
	docker-compose down

clean: ## Clean build artifacts
	mvn clean
	rm -rf target/

fix-style: ## Format code with Spotless
	mvn spotless:apply

check: ## Run all checks (test, lint, etc.)
	mvn verify

deploy-k8s: ## Deploy to Kubernetes
	kubectl apply -f k8s/

deploy-helm: ## Deploy with Helm
	helm upgrade --install recommendations-service ./charts/recommendations-service \
		--namespace production --create-namespace \
		--set image.repository=<REGISTRY>/recommendations-service \
		--set image.tag=latest

logs: ## View application logs
	kubectl logs -f deployment/recommendations-service

shell: ## Get shell access to running pod
	kubectl exec -it deployment/recommendations-service -- /bin/sh

