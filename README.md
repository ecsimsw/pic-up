# Pic-up
누구나 사용할 수 있는, 가장 쉬운 사진/동영상 앨범 스토리지를 만들고 있습니다.

## 기록

#### 썸네일, 원본 파일 동시 업로드

<img src="https://github.com/ecsimsw/pic-up/assets/46060746/555f6d6f-c84a-43c1-a7b6-040beedb3be9" width="600">

- 원본, 썸네일 파일을 동시 업로드한다. 모든 업로드가 성공했음을 확인하고 사용자에게 응답한다.
- 업로드에 실패하는 경우 사용자에게 그 즉시 예외를 알리되, 진행 중인 업로드는 완료 후 제거 로직이 수행된다.
- Mockito를 활용한 비동기 작업 순서, 예외 처리 과정을 테스트 코드로 검증한다.

#### 앨범 삭제

<img src="https://github.com/ecsimsw/pic-up/assets/46060746/ed024b23-02d0-4ecc-b6df-98ef5019e753" width="600">

- 앨범을 삭제하면 포함된 모든 사진, 영상 파일이 삭제된다.
- 사용자가 파일이 전부 삭제되는 시간을 기다릴 필요가 없기에 소프트딜리트 후 실제 파일 삭제는 사용자 요청 처리 주기 밖에서 처리된다.
- 무한 재시도 처리를 피하기 위해 삭제 시도 횟수를 기록한다.

#### 인증된 유저에게만 허용한 CDN 자원 

<img src="https://github.com/ecsimsw/pic-up/assets/46060746/7ee50f8c-f76c-484c-bde6-30bf70342068" width="600">

- CDN을 사용하여 WAS의 부하를 줄이고, Disk I/O 피한다.
- 인증된 사용자의 CDN URL을 외부인이 사용하는 경우를 막기 위해, URL을 암호화하여 유효 시간, 허용 IP를 제한한다.
- WAS에서는 private key로 암호화, CDN에서는 public key로 URL를 복호화한다.
- 유효 시간을 벗어난 요청, 범위를 벗어난 자원 요청, 허용되지 않은 IP에서의 요청에 403을 반환한다.
- {IP:URL}으로 캐싱하여 매번 새로 암호화하는 것을 피하고 Browser cache를 가능하게 한다.

#### 리버스 프록시, 응답 시간 모니터링

<img src="https://github.com/ecsimsw/pic-up/assets/46060746/cef4a3e6-6ec3-4928-8d79-1ca1d473691e" width="600">

- WAS 전면에 웹 서버를 두어 TLS, HTTP2.0, RateLimit, 정적 자원 호스팅을 처리한다.
- Nginx의 응답 시간을 모니터링 하고자 했고, Nginx 공식 Prometheus exporter으로는 가능하지 않았다.
- 액세스 로그에 요청 처리 시간을 기록하고, 액세스 로그를 파싱하는 Exporter를 사용하는 것으로 응답 속도를 모니터링 할 수 있었다.
- K6 로 부하테스트를 진행하고 WAS의 응답 속도 지표와 비교한다. (10m, 500vus, avg=138ms, med=125mx, p(95)=160ms)

#### 분산락
- 사용자마다 스토리지 사용량을 기록하고, 사용 가능 공간을 제한한다.
- 동시 업로드 요청 시 스토리지 사용량의 최종 수정 사항만 기록되는 두 번의 갱실 분실 문제 발생했다.
- 처음에는 비관적락으로 트랜잭션간 격리를 만들었지만, 충돌 시 DB 커넥션 점유가 서비스 전체 동시성 저하로 이어졌다.
- Redis의 원자적 연산으로 락을 구현하는 것으로 커넥션 점유 시간과 DB 액세스 횟수 개선할 수 있었다.
- 사용자별 락으로 서비스 전체 동시성 저하 문제를 해결하고, 썸네일 생성 등 임계구역과 상관없는 로직을 락 범위에서 분리하여 동시성을 개선한다.

#### 테스트 환경
- 가급적 좁은 범위, 테스트간 의존성이 적은 테스트 코드와 구조를 만드려 했다.
- Mockito를 사용하여 테스트 범위 외 객체, 상황을 제어한다.
- 캐시를 활용하여 Test application context 사용을 최소화하고 테스트 시간을 줄인다. (hitCount = 579, missCount = 3)
- Mockito, H2, Embedded redis, S3Mock으로 테스트 범위 외 환경을 제어한다. 다른 외부 환경없이 테스트가 수행된다.

#### 페이지 로딩 시간 개선
- 앨범 내 사진, 영상이 많고 사이즈가 커서 로딩 시간이 길었고, 모바일의 경우 파일 일부만 표시되는 문제가 발생했다.
- 업로드된 사진을 일정 비율로 축소하고, 동영상의 일부 프레임을 캡쳐하여 썸네일을 생성한다.
- 모바일 환경과 데스크탑 환경에 사용하는 사진을 달리하는 것으로 페이지에 표시되는 정적 자원의 크기 축소
- LCP를 5초에서 1.5로, 페이지 로딩 시간을 6초에서 0.5 ~ 1초로 개선

## 비용

- 달에 3.3USD 예상

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
  
</br>

## Stacks
- 프레임워크 : Java 17, Spring boot 2.7, Hibernate
- 스토리지 : Mysql, Redis, S3, NFS
- 테스트 환경 : JUnit5, Mockito, H2, S3Mock, Embedded Redis
- 인프라 : Nginx, S3, Cloudfront, Route53(GSLB), Docker, Terraform
- 모니터링 : Prometheus, Grafana, K6

## 미리보기

![미리보기](https://github.com/ecsimsw/pic-up/assets/46060746/a99d129c-cb66-433d-b680-3960b3fa002f)
