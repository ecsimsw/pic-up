#!/bin/bash

PROJECT_NAME=$1
SPRING_PROFILE=$2
PROJECT_VERSION=$(./gradlew -q printRootVersion)
CONTAINER_REGISTRY_URL="ghcr.io/ecsimsw/picup/${PROJECT_NAME}"

echo "build : $PROJECT_NAME"
echo "version : $PROJECT_VERSION"
echo "registry : $CONTAINER_REGISTRY_URL"
echo "jar : $PROJECT_NAME/build/libs/$PROJECT_NAME-$PROJECT_VERSION.jar"

./gradlew :$PROJECT_NAME:build -x test

docker build --build-arg PROJECT_VERSION="$PROJECT_VERSION" \
             --build-arg PROJECT_NAME="$PROJECT_NAME" \
             --build-arg JAR_FILE_PATH="$PROJECT_NAME/build/libs/$PROJECT_NAME-$PROJECT_VERSION.jar" \
             --build-arg SPRING_PROFILE="$SPRING_PROFILE" \
             -t "$CONTAINER_REGISTRY_URL" .

docker push "$CONTAINER_REGISTRY_URL"