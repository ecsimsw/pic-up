## Api server

### 1. api-core
- Alert utils / slack alert
- Auth utils / access token, refresh token
- Encryption utils / AES256, SHA256
- Logging utils / http access log, response time log
- Message broker utils / rabbit mq configuration

### 2. api-member [8082]
- Sign in, sign up
- User information

### 3. api-storage [8083]
- File upload, delete
- File read
- Map logical resource path to physical resource path
- Persist file history
- File encrypt / descript

### 4. api-album [8084]
- Persist User album, picture information
- Authorization

</br></br>

## Architecture

<img width="1263" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/b60fc9b9-5bfa-4502-8013-7a1aea59772e">

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
