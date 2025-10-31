.PHONY: build test run docker-build docker-run k8s-deploy k8s-delete clean

build:
	mvn clean package -DskipTests

test:
	mvn test

run:
	mvn spring-boot:run -Dspring-boot.run.profiles=local

docker-build:
	docker build -t experimentation-service:latest .

docker-run:
	docker run -p 8080:8080 \
		-e SPRING_PROFILES_ACTIVE=local \
		-e REDIS_HOST=host.docker.internal \
		-e REDIS_PORT=6379 \
		experimentation-service:latest

k8s-deploy:
	kubectl apply -f k8s/

k8s-delete:
	kubectl delete -f k8s/

helm-install:
	helm install experimentation-service charts/experimentation-service

helm-upgrade:
	helm upgrade experimentation-service charts/experimentation-service

helm-uninstall:
	helm uninstall experimentation-service

clean:
	mvn clean
	docker rmi experimentation-service:latest || true

