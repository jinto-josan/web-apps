.PHONY: help build test clean run docker-build docker-run k8s-deploy k8s-delete helm-install helm-uninstall

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-15s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

build: ## Build the application
	mvn clean package -DskipTests

test: ## Run tests
	mvn test

test-integration: ## Run integration tests
	mvn verify -P integration-test

clean: ## Clean build artifacts
	mvn clean
	docker-compose down -v

run: ## Run the application locally
	java -jar target/content-id-service-1.0.0-SNAPSHOT.jar --spring.profiles.active=local

docker-build: ## Build Docker image
	docker build -t content-id-service:latest .

docker-run: ## Run Docker container
	docker run -p 8080:8080 \
		-e DATABASE_URL=jdbc:postgresql://host.docker.internal:5432/contentid \
		-e REDIS_HOST=host.docker.internal \
		content-id-service:latest

docker-compose-up: ## Start local emulators
	docker-compose up -d

docker-compose-down: ## Stop local emulators
	docker-compose down

k8s-deploy: ## Deploy to Kubernetes
	kubectl apply -f k8s/

k8s-delete: ## Delete from Kubernetes
	kubectl delete -f k8s/

helm-install: ## Install Helm chart
	helm install content-id-service charts/content-id-service/

helm-upgrade: ## Upgrade Helm chart
	helm upgrade content-id-service charts/content-id-service/

helm-uninstall: ## Uninstall Helm chart
	helm uninstall content-id-service

flyway-migrate: ## Run Flyway migrations
	mvn flyway:migrate

flyway-info: ## Show Flyway migration info
	mvn flyway:info

