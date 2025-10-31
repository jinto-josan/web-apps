.PHONY: build test clean run docker-build docker-push deploy local-db local-redis

# Build
build:
	mvn clean package -DskipTests

# Test
test:
	mvn test

# Clean
clean:
	mvn clean

# Run locally
run:
	mvn spring-boot:run -Dspring-boot.run.profiles=local

# Docker
docker-build:
	docker build -t edge-cdn-control-service:latest .

docker-push:
	docker tag edge-cdn-control-service:latest acr.azure.io/youtube-platform/edge-cdn-control-service:latest
	docker push acr.azure.io/youtube-platform/edge-cdn-control-service:latest

# Local services
local-db:
	docker run -d --name postgres-edgecdn \
		-e POSTGRES_DB=edgecdncontrol \
		-e POSTGRES_USER=postgres \
		-e POSTGRES_PASSWORD=postgres \
		-p 5432:5432 \
		postgres:15-alpine

local-redis:
	docker run -d --name redis-edgecdn \
		-p 6379:6379 \
		redis:7-alpine

local-services: local-db local-redis

stop-local-services:
	docker stop postgres-edgecdn redis-edgecdn || true
	docker rm postgres-edgecdn redis-edgecdn || true

# Database migrations (if using Flyway)
migrate:
	mvn flyway:migrate

# Kubernetes
deploy:
	kubectl apply -f k8s/

undeploy:
	kubectl delete -f k8s/

# Helm
helm-install:
	helm install edge-cdn-control-service ./charts/edge-cdn-control-service \
		--namespace youtube-platform \
		--create-namespace \
		--set config.azure.subscriptionId=$(AZURE_SUBSCRIPTION_ID) \
		--set config.azure.tenantId=$(AZURE_TENANT_ID)

helm-upgrade:
	helm upgrade edge-cdn-control-service ./charts/edge-cdn-control-service \
		--namespace youtube-platform

helm-uninstall:
	helm uninstall edge-cdn-control-service --namespace youtube-platform

# Linting
lint:
	mvn spotless:check

format:
	mvn spotless:apply

# Coverage
coverage:
	mvn jacoco:report

