# Api server

## Architecture

<img width="1384" alt="image" src="https://github.com/ecsimsw/pic-up/blob/main/infra-docs/application-arch.png?raw=true">

</br></br>

```
http://localhost:8082/static/html/album-list.html
```

## How to configure dev env
```
docker-compose -f ./docker-compose-dev.yaml up -d

./gradlew :api-storage:build
java -jar -Dserver.port=8083 ./api-storage/build/libs/api-storage.jar --spring.profiles.active=dev

./gradlew :api-album:build
java -jar -Dserver.port=8084 ./api-album/build/libs/api-album.jar --spring.profiles.active=dev

docker-compose -f ./docker-compose-dev.yaml down
```
