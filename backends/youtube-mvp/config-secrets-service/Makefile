.PHONY: help build test run docker-build docker-run clean install

help: ## Show this help message
	@echo 'Usage: make [target]'
	@echo ''
	@echo 'Available targets:'
	@awk 'BEGIN {FS = ":.*?## "} /^[a-zA-Z_-]+:.*?## / {printf "  %-15s %s\n", $$1, $$2}' $(MAKEFILE_LIST)

build: ## Build the application
	mvn clean package -DskipTests

test: ## Run all tests
	mvn test

test-integration: ## Run integration tests
	mvn verify

run: ## Run the application locally
	mvn spring-boot:run -Dspring-boot.run.profiles=local

docker-build: ## Build Docker image
	docker build -t config-secrets-service:latest .

docker-run: ## Run Docker container
	docker-compose up -d postgres redis
	docker run -p 8080:8080 \
		--network host \
		-e DATABASE_URL=jdbc:postgresql://localhost:5432/configsecrets \
		-e DATABASE_USERNAME=postgres \
		-e DATABASE_PASSWORD=postgres \
		-e REDIS_HOST=localhost \
		-e REDIS_PORT=6379 \
		config-secrets-service:latest

docker-compose-up: ## Start Docker Compose services
	docker-compose up -d

docker-compose-down: ## Stop Docker Compose services
	docker-compose down

clean: ## Clean build artifacts
	mvn clean
	docker-compose down -v

install: ## Install dependencies
	mvn clean install -DskipTests

lint: ## Run code formatting
	mvn spotless:apply

k8s-deploy: ## Deploy to Kubernetes
	kubectl apply -f k8s/

k8s-undeploy: ## Undeploy from Kubernetes
	kubectl delete -f k8s/

helm-install: ## Install Helm chart
	helm install config-secrets-service ./charts/config-secrets-service \
		--set secrets.databaseUrl="jdbc:postgresql://postgres:5432/configsecrets" \
		--set secrets.appConfigEndpoint="$(AZURE_APP_CONFIG_ENDPOINT)" \
		--set secrets.keyVaultUri="$(AZURE_KEY_VAULT_URI)"

helm-uninstall: ## Uninstall Helm chart
	helm uninstall config-secrets-service

docker-compose.yml:
	echo "version: '3.8'" > docker-compose.yml
	echo "services:" >> docker-compose.yml
	echo "  postgres:" >> docker-compose.yml
	echo "    image: postgres:15-alpine" >> docker-compose.yml
	echo "    environment:" >> docker-compose.yml
	echo "      POSTGRES_DB: configsecrets" >> docker-compose.yml
	echo "      POSTGRES_USER: postgres" >> docker-compose.yml
	echo "      POSTGRES_PASSWORD: postgres" >> docker-compose.yml
	echo "    ports:" >> docker-compose.yml
	echo "      - \"5432:5432\"" >> docker-compose.yml
	echo "  redis:" >> docker-compose.yml
	echo "    image: redis:7-alpine" >> docker-compose.yml
	echo "    ports:" >> docker-compose.yml
	echo "      - \"6379:6379\"" >> docker-compose.yml

