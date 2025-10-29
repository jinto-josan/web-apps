APP=notifications-service
VERSION=0.0.1-SNAPSHOT
IMAGE?=notifications-service:local

.PHONY: build test docker

build:
	mvn -q -DskipTests package

test:
	mvn -q test

docker:
	docker build -t $(IMAGE) .


