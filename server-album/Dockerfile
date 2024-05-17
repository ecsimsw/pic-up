FROM openjdk:17-alpine
COPY build/libs/server-album-2.0.2.jar server-album-2.0.2.jar
ENTRYPOINT ["java","-jar","-Dspring.profiles.active=prod", "/server-album-2.0.2.jar"]