# docker build -t ghcr.io/ecsimsw/picup/server-album .
# docker push ghcr.io/ecsimsw/picup/server-album

FROM ubuntu:22.04
RUN apt-get update -y
ARG JAVA_VERSION=openjdk-17-jre-headless
RUN apt-get install -y $JAVA_VERSION
ENV JAVA_HOME=/usr/lib/jvm/java-17-openjdk-amd64
COPY build/libs/server-album-2.0.2.jar /server-album.jar
CMD ["/bin/bash", "-c", "cat /vault/secrets/picup && source /vault/secrets/picup && java -jar /server-album.jar"]