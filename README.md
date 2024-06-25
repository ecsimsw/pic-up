## Pic-up
누구나 사용할 수 있는, 가장 쉬운 사진/동영상 앨범 스토리지를 만들고 있습니다.

## Architecture

<img src = "https://github.com/ecsimsw/pic-up/assets/46060746/c425dfdb-6642-4ccf-a7ce-aa394765e595" width="620px">

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
- 동시 업로드 요청 시, 두 번의 갱신 분실 문제로 스토리지 사용량 기록에 오차가 발생했다.
- 처음에는 비관적 락으로 "조회-수정"의 전체를 다른 트랜잭션과 격리했다.
- 조회에 "Select for update"를 사용하여 배타락을 걸고, DB 인덱스를 사용자 컬럼에 추가하여 사용자 별락(Row lock)을 처리했다.
- 비관적 락은 충돌 시 커넥션을 점유하고, 이에 CP의 커넥션 부족으로 이어질 여지가 있었다.
```
# 두개의 스레드에서, 동시에 5MB 파일을 업로드할 때
Thread 1 - 현재 스토리지 사용량 조회 0MB
Thread 2 - 현재 스토리지 사용량 조회 0MB
Thread 1 - 스토리지 사용량 기록 5MB
Thread 2 - 스토리지 사용량 기록 5MB
```
- 레디스의 원자적 연산을 이용한 분산락으로 사용자별 격리, DB 커넥션 점유 시간을 낮췄다.
- 이때 트랜잭션 범위보다 락 범위를 크게 하여, 트랜잭션 커밋 전에 락이 풀려 그 사이에 다른 트랜잭션이 참여하는 경우가 발생하지 않도록 주의했다.
- 처음에는 Spring data redis의 기본 Redis client, Lettuce로 스핀-락을 구현했지만 레디스 액세스가 잦았고 락 조회 간격을 잡는 것도 쉽지 않았다.
- Redisson은 Redis pub/sub을 사용하여 락 해제시 이벤트를 발생시키고, subscribe 중인 대기자들이 이를 읽어 락 획득을 시도하게 된다.
- 스핀-락 방식의 락 점유 조회가 불필요해져 레디스 액세스 횟수가 개선되었고, 조회 간격이 사라져 락 성능을 개선되었다.
``` java
return userLockService.<Long>isolate(
    userId,
    () -> pictureFacadeService.upload(userId, albumId, resourceKey)
);
```

### [#45](https://github.com/ecsimsw/pic-up/issues/45) 서버 간 비동기 통신, 이벤트 발행 보장
- 회원 가입 시 'Member 서버'에서 요청을 받아, 가입 정보를 'Storage 서버'로 전달해야 한다.
- 사용자는 Storage 서버의 가입 정보 처리를 기다릴 필요가 없어 비동기 방식으로 이벤트를 전달하였다.
- 처음에는 Http 통신으로 가입 정보를 전달했으나, 처리 실패 시 재시도, 타임 아웃를 직접 처리해야 하고 특히 이벤트가 유실될 수 있는 불편함이 있었다.
- 이에 RabbitMQ를 사용하여 재시도, DeadLetterQueue를 처리함으로 더 안전한 비동기 이벤트 전달을 만들었다고 생각한다.

<img src = "https://github.com/ecsimsw/pic-up/assets/46060746/38631553-ca08-4210-964a-b07608383301" width="620px">

- 이때 RabbitMQ의 서버 상태 문제가 신규 가입 전체 실패로 이어지는 문제 발생했다.
- 가능하면 가입은 처리 실패를 최소화하고자 했고, 외부 서버(RabbitMQ)의 상태 문제로 가입 전체가 실패하는 경우가 없었으면 했다.
- 이에 회원 생성 이벤트를 DB에 우선 저장하는 Transaction outbox pattern을 사용했다.
- 로직 안에서 MQ의 직접 의존이 없기 때문에 RabbitMQ의 상태 문제로 가입이 실패하지 않는다.
- 또 DB에 이벤트 내용을 기록하기에 이벤트 유실 문제가 없고, 발행 재시도하여 MQ 상태가 복구되면 MQ에 전달하지 못했던 이벤트 발행을 보장할 수 있었다.

<img src = "https://github.com/ecsimsw/pic-up/assets/46060746/ca33b345-2561-49eb-bbaf-65beff26f8d7" width="720px">

### [#31](https://github.com/ecsimsw/pic-up/issues/31) 안전한 파일 제거를 위한 배치 작업
- 앨범을 제거하면 그 안에 있는 모든 사진, 영상 파일이 삭제된다.
- 사용자가 앨범 안의 파일들이 모두 삭제되는 시간을 대기할 필요없다고 생각하여 비동기로 삭제 처리하게 되었다.
- 비동기 처리를 위해 또 다른 스레드를 이용할 경우, 요청마다 추가 스레드가 필요하고 스레드 비정상 종료 등 예외 처리가 까다로워 좀 더 안전한 방법을 고민했다.
- 삭제 로직 안에서 파일을 직접 삭제하지 않고, 삭제할 파일을 DB에 표시해두고 사용자 응답을 마치는 식으로 구조를 변경했다.
- 회원 가입 로직에서 외부 서버 상태에 대한 의존을 제거하여 가입 실패를 최소화하고, 동시에 필수 이벤트는 발행됨을 보장할 수 있었다.

<img width="650" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/46e69a10-a0bf-4bd0-a122-69dfcd3aea99">

- 이후 일정 시간 간격으로 삭제 표시된 파일 내역을 조회하고, 파일을 제거하는 작업을 실행하여 실제 파일을 제거하였다.
- 작업은 k8s의 cronjob을 사용하여 작업 주기마다 실행했으며, concurrencyPolicy 옵션으로 두개 이상의 작업이 동시에 수행되는 경우를 방지했다.

<img width="650" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/a50b7fbf-e5dd-4d93-b94f-b7f62f7d2582">

- 이때 Entity 파일 등, Storage-api와 Storage-batch에서 공통적으로 사용되는 정보를 공통 모듈로 만들어, 중복 코드를 제거하였다.

<img width="600" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/81f4d86e-44ac-4927-8239-b4b573823e23">

### [#40](https://github.com/ecsimsw/pic-up/issues/40) 인증된 사용자에게만 자원을 허용하기 위한 CDN URL 암호화
- CDN으로 사용자 개인 파일을 캐시하여 서버의 부하를 낮추고 응답 속도를 개선하였다.
- CDN URL을 임의로 수정하여 다른 사람의 자원을 요청하는 등, 브루트 포스 공격으로 타인의 비공개 파일이 반환되는 문제를 고민하였다.
- CDN URL을 암호화하는 cdn signed-url 방식으로 기간 외 요청 또는 인증된 사용자 IP 외 요청에 응답 방지한다.
- RSA, 비대칭 키를 사용하여 인증된 사용자 정보가 포함된 URL을 암호화하고, CDN 제공자에서 이를 검증하여 자원 반환 여부를 결정한다.
- 만약 사용자 요청마다 매번 새로 URL을 암호화한다면, 매번 암호화된 URL이 달라져 브라우저 컨텐츠 캐싱이 적용되지 않는다.
- 암호화한 URL을 사용자 별로 캐싱하여 암호화에 걸리는 시간을 피하고, 브라우저 컨텐츠 캐싱이 가능하도록 하여 페이지 로딩 속도 개선할 수 있었다.

<img src = "https://github.com/ecsimsw/pic-up/assets/46060746/48b6f6ba-5e1c-44f9-bf7c-f940af220ffc" width="620px">

### [#24](https://github.com/ecsimsw/pic-up/issues/24) 업로드 속도 개선, 썸네일 람다
- 파일 업로드 시 썸네일을 생성하여, 파일 목록 조회나 모바일 환경에서 페이지 리소스가 줄이고 페이지 로딩 시간을 크게 개선할 수 있었다.
- 반면 서버 응답 속도나 메모리 사용은 많음을 부하 테스트와 모니터링으로 확인했다.
- S3 pre-signed url을 사용하여 백엔드 서버를 거치지 않고 클라이언트가 직접 S3 파일 업로드 수행하도록 구조 변경했다.
- S3 업로드 이벤트를 트리거하는 AWS Lambda를 정의하고 썸네일 생성 로직을 추가해, 썸네일 생성 시간을 사용자 업로드 요청 처리 과정에서 분리할 수 있었다.
- 사진 파일이 백엔드를 거치지 않고 썸네일 생성을 직접 처리하지 않기에, 업로드 응답 시간이 단축되고 서버 메모리 사용률이 크게 개선되었다.
- 서버의 파일 크기 80%에 해당하는 8MB 크기의 이미지 파일을 기준으로, 작업 전에는 3.5초가 작업 후에는 1.2초로 개선되었다.

<img src = "https://github.com/ecsimsw/pic-up/assets/46060746/760ecf6c-ea61-41ba-85bc-6266bd9c7714" width="620px">

### [#38](https://github.com/ecsimsw/pic-up/issues/38) 배포 시점에 Vault secret key 주입
- DB 비밀번호, AWS 키, JWT 키 등 공개되어선 안되는 비밀 값들을 Vault로 관리한다.
- Pod 실행 시점에 Init container에서 Secret를 환경 변수로 지정한다.
- 이때 Vault 인증은 Pod의 Service account, 저장은 컨테이너 내부의 임시 메모리 공간을 사용하여 보안을 지킨다.
- 코드 내에서 모든 Vault 정보가 제거되어 개발자가 Secret 키 관리를 신경쓰지 않아도 된다.

<img src = "https://github.com/ecsimsw/pic-up/assets/46060746/27e92c09-da73-4a57-bfe0-685489d115f1" width="520px">

### [#35](https://github.com/ecsimsw/pic-up/issues/35) DB 쿼리 성능 개선, 인덱스 튜닝과 커서 기반 페이지네이션 전환
- Bulk insert와 File insert 방식으로 천만 개의 데이터를 삽입하여 자주 사용되는 쿼리의 성능을 확인했다.
- 인덱스, 커버링 인덱스를 적용하고 실행 계획으로 적용 결과를 확인했다.
- OFFSET 기반 페이지네이션에서 커서 기반 페이지네이션으로 방식을 변경하고, 기존 5분대의 조회 시간을 50ms 이내로 개선할 수 있었다.
```
SELECT A.TITLE, P.ID, P.DESCRIPTION FROM PICTURE AS P JOIN ALBUM AS A ON P.ALBUMID = A.ID
                ORDER BY A.TITLE, P.ID
                LIMIT 10 OFFSET 9000000
# 10 rows retrieved starting from 1 in 5 m 36 s 581 ms (execution: 5 m 36 s 493 ms, fetching: 88 ms)

SELECT A.TITLE, P.ID, P.DESCRIPTION FROM PICTURE AS P JOIN ALBUM AS A ON P.ALBUMID = A.ID
                WHERE A.TITLE >= 'WvdVj7GbU' AND P.ID >= 4120335
                ORDER BY A.TITLE, P.ID LIMIT 10
# 10 rows retrieved starting from 1 in 44 ms (execution: 11 ms, fetching: 33 ms)
```

### [#37](https://github.com/ecsimsw/pic-up/issues/37) k8s Rolling update 무중단 배포
- k8s deployment의 Rolling update 방식으로 배포한다.
- 모니터링과 부하테스트를 통해 서비스 운영 도중 배포 또는 스케일 변경 시 다운 타임이 발생하는 것을 확인할 수 있었다.
- WAS에서 Graceful shutdown을 처리하여 서버 종료 시 처리 중인 요청을 완료 후 종료되도록 설정하였다.
- Pod container 종료가 LB(svc)의 라우팅 규칙 업데이트를 앞서는 경우, LB에 의해 전달된 요청이 처리되지 못한다.
- LB(svc)의 라우팅 규칙이 WAS 제거보다 먼저 처리하여 제거된 Container로의 요청 전달이 발생하지 않도록 보장한다.

### Infrastructure

<img width="550" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/5a890763-d597-4c1b-8029-e4bd6ea63a33">
<img width="550" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/eebda44e-7555-4425-906e-a135eda905fb">

### 미리보기

![미리보기](https://github.com/ecsimsw/pic-up/assets/46060746/a99d129c-cb66-433d-b680-3960b3fa002f)
