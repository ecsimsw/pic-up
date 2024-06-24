## Pic-up
누구나 사용할 수 있는, 가장 쉬운 사진/동영상 앨범 스토리지를 만들고 있습니다.

### Application

<img width="550" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/5745f0ef-fd34-4b39-9115-1a4dbed5b3fc">

- 통합테스트 외부 환경 의존 제거, 테스트 멱등성과 @Transactional : [#30](https://github.com/ecsimsw/pic-up/issues/30)     
- Dropzone, Raw binary data 전송 : [#29](https://github.com/ecsimsw/pic-up/issues/29)
- 파일 업로드 속도 개선, 클라이언트 S3 직접 업로드와 썸네일 생성 Lambda 제작 : [#24](https://github.com/ecsimsw/pic-up/issues/24)     

### [#42](https://github.com/ecsimsw/pic-up/issues/42) 트랜잭션 간 격리, DB 커넥션 점유 시간 개선
- 동시 업로드 요청 시, 두 번의 갱신 분실 문제로 스토리지 사용량 기록에 오차가 발생한다.
```
# 두개의 스레드에서, 동시에 5MB 파일을 업로드할 때
Thread 1 - 현재 스토리지 사용량 조회 0MB
Thread 2 - 현재 스토리지 사용량 조회 0MB
Thread 1 - 스토리지 사용량 기록 5MB
Thread 2 - 스토리지 사용량 기록 5MB
```
- 비관적 락으로 사용자별 격리했으나, 충돌 시 커넥션 점유가 CP 전체의 커넥션 부족으로 이어질 여지가 있었다.
- 레디스의 원자적 연산을 이용한 분산락으로 사용자별 격리, DB 커넥션 점유 시간을 낮춘다.
- 이떄 트랜잭션 범위보다 락 범위를 크게 하여, 트랜잭션 커밋 전에 락이 풀려 그 사이에 두번의 갱신 분실 문제가 발생하는 경우를 피할 수 있었다.
- 스핀락 방식이 아닌, Redisson의 pub/sub lock을 사용하여 redis 액세스 횟수를 낮추고 락 성능을 개선한다.
``` java
return userLockService.<Long>isolate(
    userId,
    () -> pictureFacadeService.upload(userId, albumId, resourceKey)
);
```

### [#45](https://github.com/ecsimsw/pic-up/issues/45) 서버 간 비동기 통신, 이벤트 발행 보장
- 회원 가입 로직에 MQ가 사용되어, RabbitMQ의 서버 상태 문제가 신규 가입 전체 실패로 이어지는 문제 발생했다.
- 외부 서버(RabbitMQ)의 상태 문제로 가입 전체가 실패하는 경우를 막고자 했다.
- 회원 생성 이벤트를 DB에 우선 저장하는 Transaction outbox pattern을 사용했다.
- 로직 안에서 MQ의 직접 의존이 없기 때문에 RabbitMQ의 상태 문제로 가입이 실패하지 않는다.
- 또 DB에 이벤트 내용을 기록하기에 이벤트 유실 문제가 없고, 발행 재시도하여 MQ 상태가 복구되면 MQ에 전달하지 못했던 이벤트 발행을 보장할 수 있었다.

<img src = "https://github.com/ecsimsw/pic-up/assets/46060746/ca33b345-2561-49eb-bbaf-65beff26f8d7" width="620px">

### [#31](https://github.com/ecsimsw/pic-up/issues/31) 안전한 파일 제거를 위한 배치 작업과 모듈 분리   
- 다중 파일 삭제 처리에서 사용자가 파일 삭제 시간을 기다릴 필요는 없다고 생각하여 비동기 처리 계획했다.
- 스레드를 통한 비동기 처리 시, 요청마다 추가 스레드가 필요했고, 스레드 비정상 종료 등 예외 처리가 까다로워 좀 더 안전한 방법을 고민했다.
- 삭제 로직 안에서 파일을 직접 삭제하지 않고 소프트 딜리트하고 사용자 응답을 마친다.
- 이후 일정 시간 간격으로 소프트 딜리트 내역을 조회하고 실제 파일을 제거하는 작업을 실행하여 사용자 요청 처리 주기 밖에서 파일을 삭제한다.
- 작업은 k8s의 cronjob을 사용하여 작업 주기마다 실행을 처리했으며, concurrencyPolicy 옵션으로 두개 이상의 작업이 동시에 수행되는 경우를 방지했다.

### [#40](https://github.com/ecsimsw/pic-up/issues/40) 인증된 사용자에게만 자원을 허용하기 위한 CDN URL 암호화
- CDN으로 사용자 개인 파일을 호스팅했고, URL 브루트 포스 공격으로 타인의 비공개 파일이 반환되는 문제가 발생했다.
- CDN URL을 암호화하고 CloudFront에서 이를 검증하여, 기간 외 요청 또는 인증된 사용자 IP 외 요청에 응답 방지한다.
- 이때 사용자 요청마다 매번 새로 URL을 암호화하게 되면 암호화에 필요한 시간이 소요되고, URL이 달라져 브라우저 컨텐츠 캐싱이 적용되지 않는다.
- 암호화한 URL을 사용자 별로 캐싱하여 암호화에 걸리는 시간을 피하고, 브라우저 컨텐츠 캐싱이 가능하도록하여 페이지 로딩 속도 개선할 수 있었다.

### Infrastructure

<img width="550" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/eebda44e-7555-4425-906e-a135eda905fb">

- 배포 시점에 Vault secret key 주입, Vault kubernetes injector : [#38](https://github.com/ecsimsw/pic-up/issues/38)
- k8s Rolling update 무중단 배포, 내장 톰캣 Graceful shutdown 동작 원리 : [#37](https://github.com/ecsimsw/pic-up/issues/37)     
- 부하 테스트로 배포 리소스 크기 결정 : [#36](https://github.com/ecsimsw/pic-up/issues/36)
- DB 쿼리 성능 개선, 인덱스 튜닝과 커서 기반 페이지네이션 전환 : [#35](https://github.com/ecsimsw/pic-up/issues/35)
- 이미지 파일 업로드 부하테스트, 응답 시간과 메모리 변화 확인 : [#19](https://github.com/ecsimsw/pic-up/issues/19)     

### Stacks
- Framework : Java 17, Spring boot 2.7, Hibernate, JUnit5, Mokito
- BE tools : Mysql, Redis, RabbitMQ, Flyway, Thumbnailator, Jcodec
- BE dev env : S3Mock, Embedded Redis, H2
- FE library : Dropzone, lightGallery, video-js
- Container, VM : Kubernetes, Docker, vagrant
- Cloud : S3, RDS, Cloudfront, Route53, Lambda, Terraform
- Monitoring : Grafana, Prometheus, Loki, Promtail

### 비용
1. Route53
- Hosted zone : 1개 -> 0.5USD (개당 0.5USD)
- Query 수 : 30,000 질의 -> 0.01USD (백만 쿼리당 0.4USD)
- 헬스 체크 : 0.75USD (부가 옵션 X, Non aws endpoint, 0.75USD/Mon)
2. CloudFront
- 요청 수 : 24,440 -> 0USD (프리티어, 백만 요청까지)
- 데이터 전송 크기 : 5.01GB -> 0USD (프리티어, 1024GB까지)
3. S3
- 버킷 크기 : 80GB -> 1.84USD (프리티어 5GB까지, 스탠다드 GB 당 0.023 USD)
- CloudFront 로 데이터 전송 : 0USD (비용 청구 x)
- S3에서 데이터 송신 : 0GB -> 0USD (GB 당 0.126USD)
4. 달 예상 비용 : 3.3USD

### 미리보기

![미리보기](https://github.com/ecsimsw/pic-up/assets/46060746/a99d129c-cb66-433d-b680-3960b3fa002f)
