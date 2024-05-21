FROM ubuntu:22.04

ARG PROJECT_VERSION
ARG PROJECT_NAME
ARG JAVA_VERSION=openjdk-17-jre-headless

RUN apt-get update -y
RUN apt-get install -y $JAVA_VERSION
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

COPY build/libs/${PROJECT_NAME}-${PROJECT_VERSION}.jar /${PROJECT_NAME}.jar

CMD ["/bin/bash", "-c", "cat /vault/secrets/picup && source /vault/secrets/picup && java -Dspring.profiles.active=prod -jar /${PROJECT_NAME}.jar"]