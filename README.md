## Pic-up

## Architecture

<img src = "https://github.com/ecsimsw/pic-up/assets/46060746/46427127-7aee-4ce1-8b34-7aab15321f0d" width="620px">

- 언어 : Java 17
- 프레임워크 : Spring boot 2.7, Hibernate, JUnit 5, Mokito
- DB, MQ : Mysql 8.0, Redis, RabbitMQ
- BE 개발 도구 : Flyway, Thumbnailator, JCodec, S3Mock, Embedded Redis, H2
- FE 개발 도구 : Dropzone, LightGallery, Video-js
- Container, VM 관리 도구 : Kubernetes, Docker, Vagrant
- 배포와 인프라 : S3, RDS, Cloudfront, Route53, Lambda, Vault
- 모니터링 환경 : Grafana, Prometheus, Loki

## 미리보기

![미리보기](https://github.com/ecsimsw/pic-up/assets/46060746/a99d129c-cb66-433d-b680-3960b3fa002f)

## 요구 사항
- 회원 가입, 로그인, Refresh token을 사용한 재로그인이 가능하다. 
- 회원 탈퇴 시 사용자 정보, 파일이 모두 삭제된다. 
- 앨범을 정의하고 사진, 동영상을 업로드 할 수 있다.
- 사용자 별 스토리지 사용 공간을 기록하고, 사용 가능 공간을 넘어선 업로드 요청을 허용하지 않는다.
- 이미지 리사이징, 동영상 프레임 캡쳐로 썸네일 파일을 생성한다.
- 개인 파일 URL 브루트포스 공격을 대비한다. 타인의 자원 요청에 비정상 응답을 반환한다.
- 사용자의 파일 목록을 페이지네이션으로 조회할 수 있다.
- 서버 리소스 매트릭 정보, 요청량과 응답 시간, 로그를 모니터링할 수 있다.
- 메모리 사용량에 따라 Container 개수를 자동 조정할 수 있다.
- 서비스 중 배포, 스케일 변경에도 요청 처리 실패가 발생하지 않도록 한다.
- 전면 웹 서버의 TLS, Rate limit, Ip white list로 서버를 보호한다.

## 기록

### [#42](https://github.com/ecsimsw/pic-up/issues/42) 트랜잭션 간 격리, DB 커넥션 점유 시간 개선
두 번의 갱신 분실 문제로 스토리지 사용량 기록에 오차가 발생했고, 처음에는 비관적 락으로 문제를 해결했다. DB 조회에 Row lock 을 사용하여 사용자 별 락을 구현하고, ‘조회-수정’ 작업의 순차 처리를 유도했다. 비관적 락은 동시성 문제 발생 시 대기를 위해 커넥션을 점유하기에 한 사용자가 CP 전체의 커넥션을 점유한다면, 결국 DB 커넥션이 필요한 모든 사용자가 한 사용자를 대기하는 문제가 발생할 수 있었다.

```
# 두 개의 스레드에서, 동시에 5MB 파일을 업로드할 때
Thread 1 - 현재 스토리지 사용량 조회 0MB
Thread 2 - 현재 스토리지 사용량 조회 0MB
Thread 1 - 스토리지 사용량 기록 5MB
Thread 2 - 스토리지 사용량 기록 5MB
```

이에 레디스의 ‘조회-수정’을 원자적으로 처리하는 연산을 이용한 분산락으로 격리 방식을 변경하였다. 이때 사용자 별로 레디스 키를 달리하여, 설령 한 사용자의 처리량이 많아도 다른 사용자는 이에 관계없이 요청을 수행하도록 하여 사용성을 개선할 수 있다. 트랜잭션 범위보다 락 범위를 크게 하여 커밋 전에 락이 풀려 그 사이에 다른 트랜잭션이 참여하는 경우가 발생하지 않도록 주의했다.

 처음엔 Spring data redis의 기본 레디스 클라이언트, Lettuce로 스핀 락을 구현했지만, 레디스 액세스가 잦았고 반복 간격을 잡는 것도 쉽지 않았다. 이에 레디스 Pub/Sub을 사용한 락을 지원하는 Redisson을 사용하여, 락 사용 종료 시에만 락 점유 경합을 유도하는 것으로, 레디스 액세스 횟수와 락 대기 시간을 개선할 수 있었다.
 
``` java
public void acquire(long userId) {
    var lockKey = LOCK_KEY_PREFIX + getIdHash(userId);
    var locks = redissonClient.getLock(lockKey);
    if (!locks.tryLock(LOCK_WAIT_TIME, LOCK_TTL, TimeUnit.MILLISECONDS)) {
        throw new AlbumException("Failed to get lock");
    }
}
```

### [#45](https://github.com/ecsimsw/pic-up/issues/45) 서버 간 비동기 통신, 이벤트 발행 보장
서비스 비즈니스를 고려하여 회원 가입 요청만큼은 최대한 실패 처리를 피하고, 가급적 가입 성공으로 이어질 수 있는 구조를 만들고자 하였다. 회원 가입 시, 서버 간 통신에 처음에는 Http 통신을 사용했으나, 서버 간 직접 의존을 피하고 싶었고, 특히 처리에 실패한 메시지가 유실될 수 있는 불안함이 있었다. 이에 RabbitMQ를 사용하여 서버 간 직접 의존을 제거하였다. 전달해야 하는 메시지를 MQ에 보관하는 것으로, 처리 실패와 타임 아웃 시 재시도 처리를 MQ의 역할로 위임하고, 재시도에도 실패한 메시지를 보관하는 DLQ를 구성하여 메시지 유실 문제를 대비하였다.

<img src = "https://github.com/ecsimsw/pic-up/assets/46060746/2d33f3a5-f489-43cd-b84c-1171f648bcc2" width="620px">

서버 간 의존 제거로 서버 상태 이상이 가입 실패로 이어지는 문제를 막을 수 있었지만, 이번엔 MQ에 의존이 생기기에 MQ 서버가 다운되면 결국 가입 전체가 실패하는 문제가 발생하였다. 그렇다고 MQ의 서버 상태 이상을 무시하고 정상 가입 처리한다면, MQ에 전달해야 하는 메시지가 유실되어 가입 이벤트를 처리할 수 없게 된다. 가입 로직만큼은 MQ 서버와의 의존도 제거하여 외부 서버에 상태에 의한 가입 실패를 최소화하고자 하였다.

이에 회원 생성 이벤트를 DB에 우선 저장하는 Transaction outbox pattern으로 구조를 변경하였다. 가입 로직 안에서 MQ의 직접 의존을 제거되어 MQ 서버의 문제가 가입 전체의 실패로 이어지는 경우를 막을 수 있었다. 또 전달해야 하는 가입 정보 메시지를 DB에 우선 기록하기에, MQ 서버가 다운되는 경우에도 발행 시도를 반복할 수 있었고, 서버 상태가 복  

<img src = "https://github.com/ecsimsw/pic-up/assets/46060746/3dddc915-f7b0-4cf5-8011-5567ad7320a0" width="720px">

### [#31](https://github.com/ecsimsw/pic-up/issues/31) 안전한 파일 제거를 위한 배치, 모듈 분리
사용자가 서비스를 탈퇴하면 사용자의 모든 사진, 영상 파일이 삭제된다. 사용성 개선을 위해 파일 삭제는 요청 처리 주기에서 벗어나 비동기 삭제 처리를 계획하였다. 처음에는 스레드를 이용하여 비동기 삭제 처리했지만, 요청마다 추가 스레드가 필요했고, 스레드 비정상 종료, 일부 파일 삭제 실패 등 예외처리가 까다롭다고 느꼈다. 무엇보다 사용자 파일만큼은 최대한 안전하게, 꼼꼼한 예외처리로 다루고 싶었다.

삭제 로직 안에서 파일을 직접 삭제하지 않고, 삭제할 파일을 DB에 표시해 두고 사용자 응답을 마치는 식으로 구조를 변경했다. 사용자의 삭제 요청 안에서는 DB 처리만 진행하고 응답하여, 사용자가 파일 제거 시간을 기다리지 않도록 하였다.

<img width="650" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/46e69a10-a0bf-4bd0-a122-69dfcd3aea99">

실제 파일 삭제는 k8s의 cronjob을 사용하여 일정 주기로 처리를 수행하였다. ConcurrencyPolicy 옵션으로 두개 이상의 작업이 동시에 스케줄링 되는 경우를 방지했다.  

<img width="650" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/a50b7fbf-e5dd-4d93-b94f-b7f62f7d2582">

이때 Entity 파일 등, Storage-api와 Storage-batch에서 공통으로 사용되는 정보를 모듈로 분리하여 중복 코드를 제거하였다.    

<img width="600" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/81f4d86e-44ac-4927-8239-b4b573823e23">

### [#40](https://github.com/ecsimsw/pic-up/issues/40) 인증된 사용자에게만 자원을 허용하기 위한 CDN URL 암호화

CDN으로 사용자 파일을 캐싱하여 서버의 부하를 낮추고 응답 속도를 개선하였다. 이때 악의적인 사용자가 URL을 임의로 수정하여 다른 사람의 자원을 요청할 수 있기에, 브루트 포스 공격을 막을 방법을 고민하였다.

 CDN URL을 암호화하는 CloudFront 의 Signed-url 방식으로 기간 외 요청 또는 인증된 사용자 IP 외 요청에 응답 방지한다. RSA 비대칭 키를 사용하여 인증된 사용자 정보가 포함된 URL을 암호화하고, CDN 제공자에서 이를 검증하여 자원 반환 여부를 결정한다. 

요청 시마다 URL을 암호화 시, URL이 매번 달라져 브라우저의 Content-Cache를 사용할 수 없었다. 이에 암호화한 URL을 사용자별로 캐싱하여 암호화에 걸리는 시간을 피하고, 브라우저 컨텐츠 캐싱으로 페이지 로딩 속도 개선할 수 있었다.

<img src = "https://github.com/ecsimsw/pic-up/assets/46060746/48b6f6ba-5e1c-44f9-bf7c-f940af220ffc" width="620px">

### [#24](https://github.com/ecsimsw/pic-up/issues/24) 업로드 속도 개선, 썸네일 람다
파일 업로드 시 썸네일을 생성하여, 파일 목록 조회나 모바일 환경에서 페이지 로딩 시간을 크게 개선할 수 있었다. 반면 업로드 시간은 늘고, 메모리 사용량이 증가함을 모니터링을 통해 확인할 수 있었다. 썸네일 처리를 위해 백엔드 서버를 거치는 ‘FE -> BE -> S3’ 구조의 업로드였고, 파일 업로드가 두 번 진행되기에 사이즈가 큰 파일의 경우 업로드 속도가 매우 느려 사용성 저하를 걱정하였다.

 S3의 Pre-signed url을 사용하여 클라이언트에서 직접 S3 파일 업로드 수행하도록 구조 변경했다. 불필요한 업로드가 제거되어 응답 속도를 절반 이상 개선할 수 있었다. 8MB 크기의 이미지 파일을 기준으로, 3.5초의 사용자 응답 시간을, 1.2초로 개선할 수 있었다.

기존 BE에서 처리했던 썸네일 생성은, S3 업로드 이벤트를 트리거하는 AWS Lambda를 정의하여 처리하였다. 더 이상 BE 서버에서 썸네일 생성을 처리하지 않기에, 썸네일 처리 시간과 메모리 사용이 불필요해져, 업로드 응답 시간이 단축되고 서버 메모리 사용률이 크게 개선됨을 확인할 수 있었다.

<img src = "https://github.com/ecsimsw/pic-up/assets/46060746/760ecf6c-ea61-41ba-85bc-6266bd9c7714" width="620px">

### [#47](https://github.com/ecsimsw/pic-up/issues/47) DB Replication 으로 데이터 백업, DB 부하 분산
DB에 읽기 전용 Replica를 두어 데이터를 백업하고 DB 부하를 분산했다. 복제는 Mysql의 비동기 방식을 사용하였다. 데이터에 수정이 발생하는 삽입, 삭제, 수정 작업은 Master DB를 사용, 단순 조회는 Slave DB를 사용하도록 하였다. 

트랜잭션 동기화 매니저의 @Transactional의 readOnly 값을 확인하는 것으로 작업의 종류를 결정할 수 있었다. 또 LazyConnectionDataSourceProxy를 사용하여 커넥션 점유를 쿼리 실행 시점으로 미뤄, 불필요한 커넥션 점유와 점유 시간을 줄일 수 있었다.  

``` java
public DataSource dataSource() {
    var determinedDataSource = new AbstractRoutingDataSource {
        @Override
        protected Object determineCurrentLookupKey() {
            var isReadOnly = TransactionSynchronizationManager.isCurrentTransactionReadOnly();
            if (isReadOnly) {
                return SLAVE;
            }
            return MASTER;
        }
    }
    return new LazyConnectionDataSourceProxy(determinedDataSource);
}
```

### [#35](https://github.com/ecsimsw/pic-up/issues/35) DB 쿼리 성능 개선, 인덱스 튜닝과 커서 기반 페이지네이션 전환
천만 개의 데이터를 삽입하여 데이터가 쌓인 상황을 가정하고, 서버에서 자주 사용되는 쿼리의 성능을 확인했다. Bulk insert와 File insert 방식으로 DB 더미 데이터를 생성했다. 인덱스, 커버링 인덱스를 적용하고 실행 계획으로 적용 결과를 확인했다. 또 OFFSET 기반 페이지네이션에서 CURSOR 기반의 페이지네이션으로 변경하는 것으로, 기존 5분대의 조회 시간을 50ms 이내로 개선할 수 있었다. 

``` sql
SELECT A.TITLE, P.ID, P.DESCRIPTION FROM PICTURE AS P JOIN ALBUM AS A ON P.ALBUMID = A.ID
                ORDER BY A.TITLE, P.ID
                LIMIT 10 OFFSET 9000000
# 10 rows retrieved starting from 1 in 5 m 36 s 581 ms (execution: 5 m 36 s 493 ms, fetching: 88 ms)

SELECT A.TITLE, P.ID, P.DESCRIPTION FROM PICTURE AS P JOIN ALBUM AS A ON P.ALBUMID = A.ID
                WHERE A.TITLE >= 'WvdVj7GbU' AND P.ID >= 4120335
                ORDER BY A.TITLE, P.ID LIMIT 10
# 10 rows retrieved starting from 1 in 44 ms (execution: 11 ms, fetching: 33 ms)
```

### [#36](https://github.com/ecsimsw/pic-up/issues/36) 모니터링과 부하테스트
리소스 사용량, 응답 속도, 요청 수, GC 동작을 모니터링하기 위해 Grafana로 대시보드를 만들었다. WAS의 매트릭을 스크랩하기 위해 Prometheus를 사용, 로그 표시를 위해 Loki를사용했다. 

Kubernetes 환경에서 컨테이너로 동작하는 Application의 로그를 모으기 위해, 홈 서버의 특정 폴더를 NFS로 등록, Container의 볼륨으로 사용하는 것으로, 로그 데이터를 한 곳에 관리하고 모니터링에 용이한 구조로 만들 수 있었다.

<img src = "https://github.com/ecsimsw/pic-up/assets/46060746/ff6c8713-0344-4474-9a89-e6c8c84af3f3" width="920px">

부하테스트는 k6를 사용했다. 테스트 케이스를 나열하고, 목표 부하를 300명의 동시 유저, 10분간 요청으로 하여  평균 응답 속도 200ms 안팎, 힙 메모리 사용량을 85% 이하로 처리할 수 있는지, GC가 제대로 메모리를 정리하고 있는지를 확인하였다. 그에 맞춰 서버(Container) 개수와 리소스 크기를 결정할 수 있었다.

<img src = "https://github.com/ecsimsw/pic-up/assets/46060746/679ece19-ec04-4b1c-9409-b4f7b10e7d90" width="920px">

### [#38](https://github.com/ecsimsw/pic-up/issues/38) 배포 시점에 Vault secret key 주입
DB 비밀번호, AWS 키, JWT 키 등 공개되어선 안되는 비밀 값들을 파일이 아닌, Vault를 사용하여 관리할 수 있도록 하였다. 파일로 관리할 경우, Git으로 업로드되진 않는지 매번 주의해야 했고, 만약 키 값이 바뀌면 해당 키를 사용하는 모든 파일들을 추척하여 그 값을 바꿔줘야 하는 관리의 번거로움이 있었다.
 Vault를 사용하여 파일 노출이나 유실에서 벗어나면서도, 키 값이 바뀌면 Vault 내에서만 그 값을 변경하면 다른 코드나 파일을 수정하지 않아도 되므로 관리가 편하다는 이점도 함께 얻을 수 있었다.

비밀 값이나 인프라 키 값을 Vault에 저장하고, Pod 실행 시점에 Init container에서 Vault를 참조, 필요한 값을 Container 환경 변수로 등록하였다. 보다 안전하고 효율적인 키, 비밀 값 관리 방식을 만들 수 있었으며, 코드 안에 불필요한 Vault 의존을 제거할 수 있었다.

<img src = "https://github.com/ecsimsw/pic-up/assets/46060746/27e92c09-da73-4a57-bfe0-685489d115f1" width="520px">

### [#37](https://github.com/ecsimsw/pic-up/issues/37) k8s Rolling update 무중단 배포
k8s deployment의 Rolling update 방식으로 배포한다. 부하 테스트를 통해 서비스 운영 도중 배포 또는 스케일 변경 시 다운 타임이 발생하는 것을 확인할 수 있었다.

WAS에 Graceful shutdown을 처리하여 서버 종료 시 처리 중인 요청을 완료 후 종료되도록 설정하였다. Pod container 종료가 LB(svc)의 라우팅 규칙 업데이트를 앞서는 경우, LB에 의해 전달된 요청이 처리되지 못한다. LB(svc)의 라우팅 규칙이 WAS 제거보다 먼저 처리하여 제거된 Container로의 요청 전달이 발생하지 않도록 보장한다.     

``` yaml
spec:
  containers:
    - name: storage-api
      image: ghcr.io/ecsimsw/picup/storage-api:latest
      ...
      lifecycle:
        preStop:
          exec:
            command: [ "sh", "-c", "sleep 10" ]    # Ensure that the IpTable is updated before kubelet kill container.
```

## Infrastructure

<img width="550" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/eebda44e-7555-4425-906e-a135eda905fb">


