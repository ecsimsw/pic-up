## 20231016 Delete all

### 요구 사항
1. 앨범 삭제 요청이 생기면 앨범 정보와 연관된 파일들이 지워져야 한다.
2. 사용자는 서버의 파일이 모두 지워지는지 확인하지 않도록 한다.
3. DB에서 앨범 정보 제거가 완료된다면 정상 응답하고, 비동기로 파일들을 지운다.

### 처리 로직

1. 분할 요청
storage 서버에 api 호출로 제거할 리소스들을 넘긴다.
connection time out / response time 을 구분하기 위해 여러 파일을 임의의 개수 단위로 나눠 요청한다.

``` java
public void deleteAll(List<String> resources) {
    for(var resourcePart : Iterables.partition(resources, IMAGE_DELETE_API_CALL_UNIT)) {
        storageClient.deleteAll(resourcePart);
    }
}
```

2. 재시도
파일들 제거 요청은 원자성을 지키지 않는다. 파일 제거 과정에서 문제가 생기면 그 파일은 건너뛰고 다음 파일 제거를 수행한다.
``` java
public List<String> deleteAll(List<String> resourceKeys) {
    List<String> deleted = new ArrayList<>();
    for (var resourceKey : resourceKeys) {
        try {
            imageStorage.delete(resourceKey);
            deleted.add(resourceKey);
        } catch (Exception e) {
            LOG.ERROR(resourceKey);
        }
    }
    return deleted;
}
```

제거 되지 않은 파일은 제거 요청을 재시도 한다. 이때 무한정 스레드를 잡고 있지 않을 수 있도록 재시도 횟수를 지정한다.
``` java
public void deleteAll(List<String> resources, int leftRetryCnt) {
    final List<String> toBeRetried = new ArrayList<>();
    for (var resourcePart : Iterables.partition(resources, IMAGE_DELETE_ALL_API_CALL_SEG_UNIT)) {
        var deleted = callDeleteAllAPI(resourcePart);
        var failed = new ArrayList<>(Sets.difference(Sets.newHashSet(resourcePart), Sets.newHashSet(deleted)));
        toBeRetried.addAll(failed);
    }
    if (!toBeRetried.isEmpty() && leftRetryCnt > 0) {
        deleteAll(toBeRetried, leftRetryCnt - 1);
    }
    if (!toBeRetried.isEmpty() && leftRetryCnt <= 0) {
        // poll in queue
        LOGGER.error("Failed to delete resources : " + resources.size());
    }
}
```

3. 가용성

만약 storage server 자체가 정상 동작하고 있지 않으면 어떻게 대응해야할까.
아예 메시지큐나 이벤트 브로커를 둘까 고민 중이다. 그리고 그것은 HA를 확실히 하는 것이다.

물론 storage server 의 HA를 신경쓰지 않겠다는 것은 아니다. 근데 MQ의 HA를 떠올린 이유는 다음과 같다.

- 상용 서비스 사용 가능
MQ는 사용 서비스를 사용하는 것으로 HA를 직접 신경쓰지 않아도 된다.

- 스레드 점유 시간
기존에는 호출하는 쪽이 다른 쪽 서버 상태를 확인하거나 연결을 재시도하는 과정에서 그 시간동안 스레드를 점유하고 있어야 했다면,
MQ를 사용하는 경우에는 큐에 요청 정보만 넣어두면 서버 상태 확인이나, 재시도 등 이후 처리는 MQ 쪽의 역할이니 WAS 에서 스레드 점유 시간을 줄일 수 있지 않을까 한다.

- 다른 모듈도
MQ를 사용하지 않고 외부 API 호출로 모듈간 통신 한다면 모든 모듈들의 HA나 예외 처리를 위와 같은 방식으로 모두 고민해야 할 것이다.
만약 MQ를 사용한다면 MQ 하나만 HA 방법을 고민하고 다른 모듈들은 모듈별로 보다 유연하게 서버 운영 방식을 관리할 수 있을 것이다.

예를들면 storage server 와 mail server 가 있을 때 API 를 직접 호출할 경우 두 서버 모두 HA를 신경써야할텐데,
MQ를 사용하고 그 큐는 HA를 보장했다면 각 server HA를 조금 더 느슨하게 할 수 있지 않을까 생각한다.
