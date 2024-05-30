# Pic-up
누구나 사용할 수 있는, 가장 쉬운 사진/동영상 앨범 스토리지를 만들고 있습니다.

## 애플리케이션 구조

<img width="600" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/c210c0bc-2bb8-4264-9984-281ace5fefbe">

## 기록

#### 파일 삭제 이벤트와 DB 처리의 원자성 보장 

<img src="https://github.com/ecsimsw/pic-up/assets/46060746/ba535147-8e23-418d-8548-028ac85eaad3" width="600">

- 파일 삭제 로직에서 예외 발생시, DB는 롤백되나 이미 삭제한 파일은 롤백할 수 없어 원자성이 깨지는 문제가 발생한다.
- 삭제할 파일 내역을 DB에 우선 기록하고 트랜잭션에 포함시켜, 파일 이벤트와 DB 처리의 원자성을 보장할 수 있었다.
- k8s Cron job으로 배치 작업을 등록하여, 일정 주기로 소프트 삭제한 파일들을 스토리지에서 제거한다.

#### 인증된 유저에게만 허용한 CDN 자원 

<img src="https://github.com/ecsimsw/pic-up/assets/46060746/7ee50f8c-f76c-484c-bde6-30bf70342068" width="600">

- 사용자 개인 파일을 CDN으로 처리하여 서버 부하를 낮추고 Disk I/O 개선
- 타인의 자원 URL을 사용하거나, 브루트포스 공격으로 개인 파일이 반환되는 문제를 고민
- CDN URL을 암호화하고, CloudFront에서 이를 검증하여 기간 외, 인증된 사용자의 IP 외 요청 방지
- 암호화된 URL을 사용자별로 캐싱하여 매번 암호화 수행을 피하고 브라우저 컨텐츠 캐시를 가능하도록 개선

### 동시성 문제 해결과 DB 커넥션 점유 시간 개선

<img src="https://github.com/ecsimsw/pic-up/assets/46060746/4cec3c3d-8a83-4d58-a56c-666648acc673" width="600">

- 이미지 업로드 동시 요청시 두번의 갱신 분실 문제로 스토리지 사용 공간 기록에 오차 발생
- 비관적 락으로 트랜잭션간 격리를 만들었지만 충돌 시 DB 커넥션 점유가 커넥션 부족으로 이어질 여지 발견
- 레디스를 이용한 분산락으로 격리 방식을 변경하여 DB 커넥션 점유 시간과 액세스 횟수 개선
- 사용자별로 락을 달리하여 충돌 횟수를 줄이고, 임계구역과 관련없는 트랜잭션을 락 범위 밖으로 분리하여 동시성 개선

#### Nginx 로그로 응답 시간 모니터링

<img src="https://github.com/ecsimsw/pic-up/assets/46060746/cef4a3e6-6ec3-4928-8d79-1ca1d473691e" width="600">

- WAS 전면에 웹 서버를 두어 TLS, HTTP2.0, RateLimit, 정적 자원 호스팅을 처리한다.
- Nginx의 응답 시간을 모니터링 하고자 했고, Nginx 공식 Prometheus exporter으로는 가능하지 않았다.
- 액세스 로그에 요청 처리 시간을 기록하고, 액세스 로그를 파싱하는 Exporter를 사용하는 것으로 응답 속도를 모니터링 할 수 있었다.
- K6 로 부하테스트를 진행하고 WAS의 응답 속도 지표와 비교한다. (10m, 500vus, avg=138ms, med=125mx, p(95)=160ms)

#### 클라이언트에서 S3 직접 업로드
- ‘FE -> BE -> S3’ 방식의 파일 업로드 요청에서 응답 속도가 느리고 메모리 사용량이 많음을 확인
- Pre-signed url 사용하여 클라이언트에서 직접 S3 파일 업로드 수행
- 이미지 리사이징, 동영상 프레임 캡처를 처리하는 AWS 람다를 개발하여 썸네일 생성, 저장 자동화
- 파일 업로드 요청의 사용자 응답 속도를 높이고, 썸네일 제작에 필요했던 메모리 사용 제거

#### 무중단 배포

- 서버 운영 도중 수행되는 배포 과정에서 Down time 또는 비정상 요청 처리 발생
- Graceful shutdown 으로 처리 중인 요청을 모두 처리하고 서버 종료
- WAS 컨테이너 제거 전 N초를 대기하는 것으로, 라우팅 규칙 업데이트를 먼저 수행하고 서버 종료 보장

#### 페이지 로딩 시간 개선
- 앨범 내 사진, 영상이 많고 사이즈가 커서 로딩 시간이 길었고, 모바일의 경우 파일 일부만 표시되는 문제가 발생했다.
- 업로드된 사진을 일정 비율로 축소하고, 동영상의 일부 프레임을 캡쳐하여 썸네일을 생성한다.
- 모바일 환경과 데스크탑 환경에 사용하는 사진을 달리하는 것으로 페이지에 표시되는 정적 자원의 크기 축소
- LCP를 5초에서 1.5로, 페이지 로딩 시간을 6초에서 0.5 ~ 1초로 개선

## 인프라 구조

<img width="600" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/cc14bf44-50fe-49da-830a-74c534a9020b">

## 미리보기

![미리보기](https://github.com/ecsimsw/pic-up/assets/46060746/a99d129c-cb66-433d-b680-3960b3fa002f)


## Stacks
- Framework : Java 17, Spring boot 2.7, Hibernate, JUnit5, Mokito
- BE tools : Mysql, Redis, RabbitMQ, H2, Flyway
- BE libray : S3Mock, Embedded Redis, Thumbnailator, Jcodec
- FE library : Dropzone, lightGallery, video-js
- On premise : Nginx, Kubernetes, Vault, NFS, Vagrant
- Cloud : S3, Cloudfront, Route53, Lambda, Terraform
- Monitoring : Grafana, Prometheus, Loki, Promtail

## 비용
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
