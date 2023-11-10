## 20231111 Jib 로 Container image 빌드, 푸시

### 기존의 Docker 를 사용한 컨테이너 이미지 빌드

CI/CD 과정에서 컨테이너 이미지를 생성하기 위해서 도커를 사용해왔다.
1. Dockerfile 에 base image 에 레이어를 쌓아 빌드/구동에 필요한 환경을 구성하고,
2. dockerhub 나 ECR, GHCR 같은 container registry 를 Tag 에 지정하면서 docker build 한다.
3. 이 빌드한 이미지를 push 하여 registry 에 컨테이너 이미지를 올리고 이를 사용하게 된다.

이 세가지 과정을 위해 dockerfile 도 만들어줘야하고, 이 dockerfile 로 빌드하는 build agent 에 docker daemon 를 설치해야 했었다.
dockerfile 과 build 환경을 만드는데 더 많은 시간을 사용해야 했다.

```
## dockerfile 예시
FROM adoptopenjdk/openjdk11:jre-11.0.10_9-alpine
VOLUME /tmp
ARG JAR_FILE
COPY build/libs/*.jar /app.jar
ENTRYPOINT ["java","-jar","/app.jar"]

## docker build 와 push 예시
docker build -t {CONTAINER_REGISTRY}:{VERSION} {DOCKERFILE_PATH}
docker push {IMAGE_NAME}
```

### Jib 방식

"Jib builds containers without using a Dockerfile or requiring a Docker installation."
"You don't need to know best practices for creating Dockerfiles or have Docker installed."

google (https://cloud.google.com/java/getting-started/jib) 에서 JIB 를 소개하는 첫 줄이다.
Jib 를 이용하면 앞선 기존 방식의 dockerfile 를 만들거나 docker daemon 을 설치하지 않아도 된다.

게다가 애플리케이션을 종속 항목, 리소스, 클래스 등 별개의 레이어로 구성하고 이미지 레이어 캐싱을 활용해 더 빠른 빌드와 이미지 크기 축소를 가능하게 한다고 설명한다.

### Gradle 에서 Jib 사용하기

사용 방법도 엄청 간단하다.
picup 프로젝트에 맞춰 gradle 로 Jib 빌드 하는 것으로 가볍고 빠르게 Container 이미지를 빌드하고 registry 로 한번에 올리는 예제를 소개한다.

Build.gradle에 아래 plugin 과 jib 설정을 추가한다.
```
plugins {
    id 'com.google.cloud.tools.jib' version '3.1.4'
}
```

```
jib {
    from.image = "adoptopenjdk/openjdk11:jre-11.0.10_9-alpine"
    to.image = "ghcr.io/ecsimsw/picup/${project.name}"
    to.tags = ["latest"]
}
```

from image 는 dockerfile 처럼 base image 를 지정하는 것으로 나는 alphine 에 openjdk 11 jre 가 올려진 이미지로 하였다.
to.image 는 image 태그가 된다. 기존 docker build 에서 -t 로 지정하는 ''{CONTAINER_REGISTRY}'의 태그를 생각하면 될 것 같고
to.tags 로 version 등 추가 태그를 지정할 수 있다.

```
gradle jib                 // 단일 모듈
gradle :{projectName}:jib  // 멀티 모듈
```

그리고 위처럼 gradle 의 jib 명령을 실행시키는 것으로 한번에 container image 가 빌드되고 tag 값으로 registry 에 이미지가 push 된다.

```
chmod +x ./gradlew
version=$(./gradlew properties -q | grep "version:" | awk '{print $2}')
./gradlew :api-album:jib -Djib.to.tags=latest,${version}
./gradlew :api-member:jib -Djib.to.tags=latest,${version}
./gradlew :api-storage:jib -Djib.to.tags=latest,${version}
```

picup 의 github actions 에선 gradlew 를 사용하여 gradle 명령어를 실행한다.
build.gradle 에 기입한 version 정보를 가져와 변수 version 에 담고, image version 으로 그 버전 정보를 담는다.
이후 gradle jib 빌드하는 것으로 build.gradle 에 기입한 registry latest, version 정보를 담은 container image 가 빌드/푸시되게 된다.


