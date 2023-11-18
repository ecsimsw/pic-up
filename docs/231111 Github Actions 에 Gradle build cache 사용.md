##  Github action gradle cache

현재 CICD 의 전과정은 다음과 같다.
1. Git push 가 일어나면 Github actions 을 실행한다.
2. JDK 11 환경에서 Gradle 빌드한다.
3. GHCR 에 로그인한다.
4. gradlew 로 JIB 를 실행해 container image 를 만들고 ghcr 에 올린다.

여기서 gradle build 하는 과정이 오래 걸려 이를 캐시하고자 했다.

### Gradle Caching

```
- name: Gradle Caching
  uses: actions/cache@v3
  with:
    path: |
      ~/.gradle/caches
      ~/.gradle/wrapper
    key: ${{ runner.os }}-gradle-${{ hashFiles('**/*.gradle*', '**/gradle-wrapper.properties') }}
```

path는 cache를 저장하는 경로인 것 같고, key는 캐시 키를 말하는 것 같다.

{runner.os}가 바뀌면 gradle 파일이 같아도 같은 캐시 값이 아닐테니 의미가 없을 것이니 키 prefix로 github actions 이 실행되는 os를 추가하고,
grale 파일과 gradlew properties 파일들을 해시하여 postfix 하는 것으로 gradle 파일이 같음을 보장한다.

즉 ruuner.os가 다르거나 gradle 파일 중 하나라도 수정이 된다면 캐시 미스를 발생시키고, 그렇지 않고 이 세 지표가 같다면 그 파일들을 캐시로 사용하겠다는 전략이다.

### Performance

#### cache load

cache load 에서 약간의 시간이 걸린다. 당연하겠지만 그 시간이 빌드 시간보다 길면 의미가 없을 것 같다.    
나는 HA / HPA를 위해 k8s cluster에 actions-runner-controller 를 따로 띄워 runner를 실행하는데, 그래서 느린건지 더 지켜봐야겠다.    

<img width="1409" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/9c6b078e-3802-4d59-8045-16e99dc688d9">



#### build and push

적용한 프로젝트가 여러 모듈을 갖고 있고 아래 테스트 결과는 container build 이후 push 까지 한 결과이다.
가장 느린 모듈이 평균 23초에서 7초로 줄었다.
다른 모듈까지 더 더하면 그 캐시의 효과는 훨씬 줄 것이다.

모듈 중에 가장 짧은 빌드 시간인 모듈은 5초인데 만약 모듈이 하나고 빌드 시간이 이렇게 짧았으면 캐시가 더 방해됬을 거라고 생각한다.

before : max module -> avg 23s    
<img width="588" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/e31eac61-c500-40fe-8d8b-89c0a34614d4">


after : max module -> avg 7s    
<img width="599" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/a5ceca76-1fc9-40b2-b5c9-d821ec19f38a">


