.PHONY: help build test clean run-local docker-build docker-run docker-push k8s-deploy k8s-logs k8s-undeploy db-migrate db-rollback

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-15s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

build: ## Build the application JAR
	mvn clean package -DskipTests

test: ## Run all tests
	mvn test

clean: ## Clean build artifacts
	mvn clean
	rm -rf target/

run-local: ## Run locally with local profile
	mvn spring-boot:run -Dspring-boot.run.profiles=local

docker-build: ## Build Docker image
	docker build -t drm-service:latest .

docker-run: ## Run Docker container locally
	docker run --env-file .env -p 8080:8080 drm-service:latest

docker-push: ## Push Docker image to registry
	docker tag drm-service:latest <registry>/drm-service:latest
	docker push <registry>/drm-service:latest

k8s-deploy: ## Deploy to Kubernetes
	kubectl apply -f k8s/

k8s-logs: ## View Kubernetes logs
	kubectl logs -f deployment/drm-service -n youtube-mvp

k8s-undeploy: ## Undeploy from Kubernetes
	kubectl delete -f k8s/

db-migrate: ## Run database migrations
	mvn flyway:migrate

db-rollback: ## Rollback last migration
	mvn flyway:repair

docker-compose-up: ## Start local infrastructure (PostgreSQL, Redis)
	docker-compose up -d

docker-compose-down: ## Stop local infrastructure
	docker-compose down

setup-local: ## Setup local development environment
	$(MAKE) docker-compose-up
	$(MAKE) db-migrate
	@echo "Local environment ready!"

coverage: ## Generate test coverage report
	mvn jacoco:report
	@echo "Coverage report generated at: target/site/jacoco/index.html"

