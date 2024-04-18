# Api server

## Architecture



```
http://localhost:8082/static/html/album-list.html
```

## Test 

1. MockMvc 는 Spring framework 에서 제공하는 컨트롤러 테스트용 라이브러리이다.
2. @WebMvcTest 로 Controller 관련 빈들만 Context 에 띄울 수 있는데, MockBean 으로 테스트 마다 다른 빈을 주입하게 되면 Context 캐싱이 안되기 때문에 standAlone 모드를 사용한다.
3. MockMvc 는 실제 TestDispatcherServlet 을, TestDispatcherServlet 은 정의한 Controller 를 사용한다.


## How to configure dev env
```
docker-compose -f ./docker-compose-dev.yaml up -d

./gradlew :api-storage:build
java -jar -Dserver.port=8083 ./api-storage/build/libs/api-storage.jar --spring.profiles.active=dev

./gradlew :api-album:build
java -jar -Dserver.port=8084 ./api-album/build/libs/api-album.jar --spring.profiles.active=dev

docker-compose -f ./docker-compose-dev.yaml down
```
