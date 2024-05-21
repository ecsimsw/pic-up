FROM ubuntu:22.04

ARG PROJECT_VERSION
ARG PROJECT_NAME
ARG JAR_FILE_PATH
ARG SPRING_PROFILE

ARG JAVA_VERSION=openjdk-17-jre-headless
ARG TARGET_JAR_FILE_PATH="/${PROJECT_NAME}.jar"

RUN apt-get update -y
RUN apt-get install -y $JAVA_VERSION
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64

COPY $JAR_FILE_PATH $TARGET_JAR_FILE_PATH

ENV TARGET_JAR_FILE_PATH $TARGET_JAR_FILE_PATH
ENV SPRING_PROFILE $SPRING_PROFILE
CMD ["/bin/bash", "-c", "cat /vault/secrets/picup && source /vault/secrets/picup && java -Dspring.profiles.active=$SPRING_PROFILE -jar $TARGET_JAR_FILE_PATH"]