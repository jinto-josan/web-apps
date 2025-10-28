.PHONY: build run test docker-build docker-run clean

build:
	mvn clean package

run:
	mvn spring-boot:run -Dspring-boot.run.profiles=local

test:
	mvn test

docker-build:
	docker-compose build

docker-run:
	docker-compose up

docker-down:
	docker-compose down

clean:
	mvn clean
	docker-compose down -v

integration-test:
	docker-compose up -d
	sleep 10
	mvn verify
	docker-compose down

