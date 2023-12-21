# Api server

## Architecture

<img width="1384" alt="image" src="https://github.com/ecsimsw/pic-up/blob/main/infra-docs/application-arch.png?raw=true">

</br></br>

## How to configure dev env
```
docker-compose -f ./docker-compose-dev.yaml up -d

./gradlew :api-member:build
java -jar -Dserver.port=8082 ./api-products/build/libs/api-member.jar --spring.profiles.active=dev

./gradlew :api-storage:build
java -jar -Dserver.port=8083 ./api-audits/build/libs/api-storage.jar --spring.profiles.active=dev

./gradlew :api-album:build
java -jar -Dserver.port=8084 ./api-auth/build/libs/api-album.jar --spring.profiles.active=dev

docker-compose -f ./docker-compose-dev.yaml down
```
