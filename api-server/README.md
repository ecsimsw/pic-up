# Api server

## How to configure dev env
```
docker-compose -f ./docker-compose-dev.yaml up -d

./gradlew :api-member:build
java -jar -Dserver.port=8082 ./api-products/build/libs/api-member.jar --spring.profiles.active=dev

./gradlew :api-storage:build
java -jar -Dserver.port=8083 ./api-audits/build/libs/api-storage.jar --spring.profiles.active=dev

./gradlew :api-album:build
java -jar -Dserver.port=8084 ./api-auth/build/libs/api-album.jar --spring.profiles.active=dev
```

## Down docker envs
```
docker-compose -f ./docker-compose-dev.yaml down
```
