## Pic-up
누구나 사용할 수 있는, 가장 쉬운 사진/동영상 앨범 스토리지를 만들고 있습니다.

## Architecture

<img src = "https://github.com/ecsimsw/pic-up/assets/46060746/46427127-7aee-4ce1-8b34-7aab15321f0d" width="620px">

- Language : Java 17
- Framework : Spring boot 2.7, Hibernate, JUnit5, Mokito
- DB, MQ : Mysql, Redis, RabbitMQ
- BE library : Flyway, Thumbnailator, JCodec, S3Mock, Embedded Redis, H2
- FE library : Dropzone, lightGallery, video-js
- Container, VM : Kubernetes, Docker, Vagrant
- Infrastructure : S3, RDS, Cloudfront, Route53, Lambda, Vault
- Monitoring : Grafana, Prometheus, Loki

## 기록

### [#42](https://github.com/ecsimsw/pic-up/issues/42) 트랜잭션 간 격리, DB 커넥션 점유 시간 개선
동시 업로드 요청 시, 두 번의 갱신 분실 문제로 스토리지 사용량 기록에 오차가 발생했다.    
처음에는 조회에 "Select for update"를 사용하여 비관적 락으로 격리했으나, 충돌 시 커넥션을 점유하고 이는 CP의 커넥션 부족으로 이어질 여지가 있었다.    
낙관적 락으로 커넥션 점유 문제는 해결할 수 있었지만, DB 액세스가 잦고, 재시도 처리를 위한 코드로 코드 가독성이 떨어짐을 느꼈다.    
```
# 두 개의 스레드에서, 동시에 5MB 파일을 업로드할 때
Thread 1 - 현재 스토리지 사용량 조회 0MB
Thread 2 - 현재 스토리지 사용량 조회 0MB
Thread 1 - 스토리지 사용량 기록 5MB
Thread 2 - 스토리지 사용량 기록 5MB
```
레디스의 원자적 연산을 이용한 분산락으로 사용자별 격리, DB 커넥션 점유 시간을 개선했다.    
이때 트랜잭션 범위보다 락 범위를 크게 하여, 트랜잭션 커밋 전에 락이 풀려 그 사이에 다른 트랜잭션이 참여하는 경우가 발생하지 않도록 주의했다.     
처음에는 Spring data redis의 기본 Redis client, Lettuce로 스핀-락을 구현했지만, 레디스 액세스가 잦았고 반복 간격을 잡는 것도 쉽지 않았다.    
Redis Pub/Sub을 사용한 락을 지원하는 Redisson으로, 레디스 액세스 횟수와 락 대기 시간을 개선할 수 있었다.    
사용자 id 해시값을 redis key로 하여 사용자별 격리를 만들었다.      
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
회원 가입 시 유저 서버에서 요청을 받아, 가입 정보를 스토리지 서버로 전달해야 한다.     
사용자가 스토리지 서버의 처리를 대기하기에 시간이 걸리므로, 가입 정보 전달을 비동기로 처리한다.     
처음에는 Http 통신으로 가입 정보를 전달했으나 처리 실패, 타임아웃, 재시도를 직접 처리해야 헀고, 특히 이벤트가 유실될 수 있는 불편함이 있었다.      
이에 RabbitMQ를 사용하여 재시도, DeadLetter를 처리함으로 더 안전한 비동기 이벤트 전달 구조를 만들었다고 생각한다.     

<img src = "https://github.com/ecsimsw/pic-up/assets/46060746/38631553-ca08-4210-964a-b07608383301" width="620px">

가입 로직 안에서 MQ를 직접 의존하니, RabbitMQ 서버 상태에 문제가 가입 전체 실패로 이어졌다.    
그렇다고 MQ의 서버 상태 이상을 무시하고 정상 가입 처리한다면, 스토리지 서버로 전달해야 하는 가입 이벤트는 유실될 것이다.    
이에 회원 생성 이벤트를 DB에 우선 저장하는 Transaction outbox pattern을 사용했다.    
로직 안에서 MQ의 직접 의존이 없기 때문에 RabbitMQ 서버의 상태 문제가 로직의 실패로 이어지지 않는다.    
또 DB에 이벤트 내용을 기록하기에 유실 문제에서 안전하고, 이벤트 발행 시도를 반복하여 상태가 복구되면 이벤트가 MQ로 전달되어 상태 복구도 가능해졌다.    
회원 가입 로직에서 외부 서버 상태에 대한 의존을 제거하여 가입 실패를 최소화하고, 필수 이벤트의 발행을 보장할 수 있는 구조를 만들 수 있었다.    

<img src = "https://github.com/ecsimsw/pic-up/assets/46060746/3dddc915-f7b0-4cf5-8011-5567ad7320a0" width="720px">

### [#31](https://github.com/ecsimsw/pic-up/issues/31) 안전한 파일 제거를 위한 배치, 모듈 분리
사용자가 서비스를 탈퇴하거나 앨범을 제거하면 그 안의 모든 사진, 영상 파일이 삭제된다.    
사용자가 앨범 안의 파일들이 모두 삭제되는 시간을 대기해선 안된다고 생각하여, 파일 삭제는 사용자 요청 처리 주기 밖으로 한다.    
스레드를 이용한 비동기 처리는 요청마다 추가 스레드가 필요하고, 스레드 비정상 종료 등 예외처리가 까다로워 보다 안전한 방법을 고민했다.    
무엇보다 사용자의 사진, 영상 삭제 트랜잭션은 최대한 안전하게, 꼼꼼한 예외처리로 다루고 싶었다.    

<img width="650" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/46e69a10-a0bf-4bd0-a122-69dfcd3aea99">

삭제 로직 안에서 파일을 직접 삭제하지 않고, 삭제할 파일을 DB에 표시해 두고 사용자 응답을 마치는 식으로 구조를 변경했다.    
이후 일정 시간 간격으로 삭제 표시된 파일 내역을 조회하고, 파일을 제거하는 작업을 실행하여 실제 파일을 제거한다.    
작업은 k8s의 cronjob을 사용하여 작업 주기마다 실행했으며, concurrencyPolicy 옵션으로 두개 이상의 작업이 동시에 수행되는 경우를 방지했다.    

<img width="650" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/a50b7fbf-e5dd-4d93-b94f-b7f62f7d2582">

이때 Entity 파일 등, Storage-api와 Storage-batch에서 공통으로 사용되는 정보를 모듈로 분리하여 중복 코드를 제거하였다.    

<img width="600" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/81f4d86e-44ac-4927-8239-b4b573823e23">

### [#40](https://github.com/ecsimsw/pic-up/issues/40) 인증된 사용자에게만 자원을 허용하기 위한 CDN URL 암호화
CDN으로 사용자 파일을 캐시하여 서버의 부하를 낮추고 응답 속도를 개선하였다.    
이때 악의적인 사용자가 URL을 임의로 수정하여 다른 사람의 자원을 요청할 수 있기에, 브루트 포스 공격을 막을 방법을 고민했다.    
CDN URL을 암호화하는 cdn signed-url 방식으로 기간 외 요청 또는 인증된 사용자 IP 외 요청에 응답 방지한다.    
RSA 비대칭 키를 사용하여 인증된 사용자 정보가 포함된 URL을 암호화하고, CDN 제공자에서 이를 검증하여 자원 반환 여부를 결정한다.    
만약 사용자 요청마다 매번 새로 URL을 암호화한다면, 매번 암호화된 URL이 달라져 브라우저 컨텐츠 캐싱이 적용되지 않는다.    
암호화한 URL을 사용자별로 캐싱하여 암호화에 걸리는 시간을 피하고, 브라우저 컨텐츠 캐싱이 가능하게 하여 페이지 로딩 속도 개선할 수 있었다.    

<img src = "https://github.com/ecsimsw/pic-up/assets/46060746/48b6f6ba-5e1c-44f9-bf7c-f940af220ffc" width="620px">

### [#24](https://github.com/ecsimsw/pic-up/issues/24) 업로드 속도 개선, 썸네일 람다
파일 업로드 시 썸네일을 생성하여, 파일 목록 조회나 모바일 환경에서 페이지 로딩 시간을 크게 개선할 수 있었다.    
반면 서버 응답 속도나 메모리 사용은 많음을 부하 테스트와 모니터링으로 확인했다.    
S3 pre-signed url을 사용하여 백엔드 서버를 거치지 않고 클라이언트가 직접 S3 파일 업로드 수행하도록 구조 변경했다.    
S3 업로드 이벤트를 트리거하는 AWS Lambda를 정의하고 썸네일 생성을 자동화하였다.    
썸네일이 생성되면 WAS에 썸네일 생성 정보를 알리고 WAS는 그 이후부터 썸네일을 사용한다.     
WAS를 거치지 않고 직접 S3 업로드, 썸네일 생성을 Lambda에서 하기에, 업로드 응답 시간이 단축되고 서버 메모리 사용률이 크게 개선되었다.    
서버의 파일 크기 80%에 해당하는 8MB 크기의 이미지 파일을 기준으로, 작업 전에는 3.5초가 작업 후에는 1.2초로 개선되었다.    

<img src = "https://github.com/ecsimsw/pic-up/assets/46060746/760ecf6c-ea61-41ba-85bc-6266bd9c7714" width="620px">

### [#47](https://github.com/ecsimsw/pic-up/issues/47) DB Replication 으로 데이터 백업, DB 부하 분산
읽기 전용 Replica를 두어 데이터를 백업하고 DB의 요청을 분산했다.    
복제는 Mysql의 비동기 방식을 사용하였다.    
동기 방식의 경우 각 노드의 상태에 의존이 생기고, 복제되는 시간을 대기해야 하기에 사용성이 저하된다.    
Master db, Slave db를 나누고, Transactional의 readOnly 여부에 따라 datasource를 결정하였다.    
LazyConnectionDataSourceProxy를 사용하여 커넥션 점유를 쿼리 실행 시점으로 미뤄, 불필요한 커넥션 점유 빈도와 점유 시간을 줄일 수 있었다.    

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
Bulk insert와 File insert 방식으로 천만 개의 데이터를 삽입하여 자주 사용되는 쿼리의 성능을 확인했다.    
인덱스, 커버링 인덱스를 적용하고 실행 계획으로 적용 결과를 확인했다.    
OFFSET 기반 페이지네이션에서 커서 기반 페이지네이션으로 방식을 변경하고, 기존 5분대의 조회 시간을 50ms 이내로 개선할 수 있었다.    
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
Nginx와 WAS의 리소스 사용량, 응답 속도, 요청 수를 모니터링한다.    
Promethues로 Spring actuator의 매트릭을 추출, Nginx의 액세스 로그에서 요청 수와 응답 속도를 추출한다.    
Application 로그를 표시하기 위해 Loki와 Promtail을 사용했다.    
로그를 모을 파일 스토리지(홈 서버)을 NFS로 만들고 WAS의 Container 볼륨으로 이 NFS를 사용하는 것으로 여러 WAS에서 발생하는 로그 파일을 한곳으로 모을 수 있었다.     

![image](https://github.com/ecsimsw/pic-up/assets/46060746/558d7027-3bd6-465a-881f-963dc86242c5)

부하테스트는 k6를 사용했다.     
테스트 케이스를 나열하고, 목표 부하(300명의 동시 유저, 10분간 요청 반복)를 기준치 안에 처리할 수 있는지를 확인했다.    
평균 응답 속도가 200ms 안팎, 힙 메모리 사용량이 85% 이하로 처리할 수 있는지, GC가 제대로 메모리를 정리하고 있는지를 확인했다.    
메모리 요청과 제한을 500Mi으로 하는 컨테이너 2개로 배포 리소스 크기를 결정할 수 있었다.    

![image](https://github.com/ecsimsw/pic-up/assets/46060746/679ece19-ec04-4b1c-9409-b4f7b10e7d90)

### [#38](https://github.com/ecsimsw/pic-up/issues/38) 배포 시점에 Vault secret key 주입
DB 비밀번호, AWS 키, JWT 키 등 공개되어선 안되는 비밀 값들을 파일로 관리할 경우 노출의 위험이 있다.    
또, 여러 프로젝트에서 공통적으로 사용되는 키 값이 바뀌면 관련된 모든 사용처를 추척하여 내용을 바꿔줘야 한다.    
이에 비밀 값이나 앱 공통 변수를 Vault에 저장하고, Pod 실행 시점에 Secret를 환경 변수로 지정하였다.    
Vault 인증은 Pod의 Service account, 저장은 컨테이너 내부의 임시 메모리 공간을 사용하여 보안을 지킨다.    
비밀 값 관리에 용이해졌고, 공통 변수가 변경되면 Secret 하나만 바꾸면 되기에 변경에 유연해졌다.    
무엇보다 개발자가 키 관리를 신경 쓰지 않고 App 내에서 Vault 의존이 제거되었다.     

<img src = "https://github.com/ecsimsw/pic-up/assets/46060746/27e92c09-da73-4a57-bfe0-685489d115f1" width="520px">

### [#37](https://github.com/ecsimsw/pic-up/issues/37) k8s Rolling update 무중단 배포
k8s deployment의 Rolling update 방식으로 배포한다.    
모니터링과 부하 테스트를 통해 서비스 운영 도중 배포 또는 스케일 변경 시 다운 타임이 발생하는 것을 확인할 수 있었다.    
WAS에서 Graceful shutdown을 처리하여 서버 종료 시 처리 중인 요청을 완료 후 종료되도록 설정하였다.     
Pod container 종료가 LB(svc)의 라우팅 규칙 업데이트를 앞서는 경우, LB에 의해 전달된 요청이 처리되지 못한다.
LB(svc)의 라우팅 규칙이 WAS 제거보다 먼저 처리하여 제거된 Container로의 요청 전달이 발생하지 않도록 보장한다.     

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

## 미리보기

![미리보기](https://github.com/ecsimsw/pic-up/assets/46060746/a99d129c-cb66-433d-b680-3960b3fa002f)
