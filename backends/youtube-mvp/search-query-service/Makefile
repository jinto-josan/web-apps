.PHONY: help build test run-local local-start local-stop clean

help:
	@echo "Available targets:"
	@echo "  build          - Build the application"
	@echo "  test           - Run unit tests"
	@echo "  test-all       - Run all tests"
	@echo "  run-local      - Run locally with emulators"
	@echo "  local-start    - Start local emulators"
	@echo "  local-stop     - Stop local emulators"
	@echo "  docker-build   - Build Docker image"
	@echo "  docker-run     - Run Docker container"
	@echo "  clean          - Clean build artifacts"

build:
	mvn clean package -DskipTests

test:
	mvn test

integration-test:
	mvn test -Dtest=*IntegrationTest

test-all:
	mvn clean test

run-local:
	mvn spring-boot:run -Dspring-boot.run.profiles=local

local-start:
	docker-compose up -d

local-stop:
	docker-compose down

docker-build:
	docker build -t search-query-service:latest .

docker-run:
	docker run -p 8080:8080 search-query-service:latest

clean:
	mvn clean
	rm -rf target/

# Helm targets
helm-install:
	helm install search-query-service ./charts/search-query-service

helm-upgrade:
	helm upgrade search-query-service ./charts/search-query-service

helm-uninstall:
	helm uninstall search-query-service

# Debug targets
logs:
	kubectl logs -f deployment/search-query-service

port-forward:
	kubectl port-forward svc/search-query-service 8080:80
