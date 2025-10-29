.PHONY: build run test docker

build:
	mvn -q -DskipTests package

test:
	mvn -q test

run:
	mvn -q spring-boot:run -Dspring-boot.run.profiles=local

docker:
	mvn -q -DskipTests package && docker build -t ads-decision-service:local .


