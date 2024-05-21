#!/bin/bash

PROJECT_NAME=member-api
PROJECT_VERSION=$(../gradlew properties -q | awk '/^version:/ {print $2}')
CONTAINER_REGISTRY_URL=ghcr.io/ecsimsw/picup/"$PROJECT_NAME"

../gradlew :"$PROJECT_NAME":build -x test

docker build --build-arg PROJECT_VERSION="$PROJECT_VERSION" \
             --build-arg PROJECT_NAME="$PROJECT_NAME" \
             -t "$CONTAINER_REGISTRY_URL" .

docker push "$CONTAINER_REGISTRY_URL"