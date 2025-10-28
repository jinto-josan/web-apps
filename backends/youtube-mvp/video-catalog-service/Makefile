.PHONY: help build test run clean docker-build docker-push k8s-deploy k8s-undeploy

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-15s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

build: ## Build the application
	mvn clean package -DskipTests

test: ## Run tests
	mvn test

integration-test: ## Run integration tests
	mvn verify

run: ## Run the application locally
	mvn spring-boot:run -Dspring-boot.run.profiles=local

clean: ## Clean build artifacts
	mvn clean

docker-build: ## Build Docker image
	docker build -t video-catalog-service:latest .

docker-push: ## Push Docker image to registry
	docker push <REGISTRY>/video-catalog-service:1.0.0

k8s-deploy: ## Deploy to Kubernetes
	kubectl apply -f k8s/deployment.yaml
	kubectl apply -f k8s/secrets.yaml

k8s-undeploy: ## Undeploy from Kubernetes
	kubectl delete -f k8s/deployment.yaml

logs: ## View pod logs
	kubectl logs -f -l app=video-catalog-service -n youtube-mvp

shell: ## Shell into pod
	kubectl exec -it -l app=video-catalog-service -n youtube-mvp -- /bin/sh

check: ## Run linting and code quality checks
	mvn spotless:check

format: ## Format code
	mvn spotless:apply

