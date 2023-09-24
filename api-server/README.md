# Api server

## Service architecture

<img width="932" alt="image" src="https://github.com/ecsimsw/A-to-Z/assets/46060746/b165295c-5c83-4bad-845b-73ce85f77562">

## CI/CD flow

<img width="932" alt="image" src="https://github.com/ecsimsw/A-to-Z/assets/46060746/23bedbc7-f88b-43a7-8e2d-ee9c30e7264d">

## How to configure dev env
```
docker-compose -f ./docker-compose-dev.yaml up -d

./gradlew :api-products:build
java -jar -Dserver.port=8080 ./api-products/build/libs/api-products.jar --spring.profiles.active=dev

./gradlew :api-audits:build
java -jar -Dserver.port=8081 ./api-audits/build/libs/api-audit.jar --spring.profiles.active=dev

./gradlew :api-auth:build
java -jar -Dserver.port=8082 ./api-auth/build/libs/api-auth.jar --spring.profiles.active=dev
```

## Down docker envs
```
docker-compose -f ./docker-compose-dev.yaml down
```
