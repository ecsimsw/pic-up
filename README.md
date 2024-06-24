## Pic-up
누구나 사용할 수 있는, 가장 쉬운 사진/동영상 앨범 스토리지를 만들고 있습니다.

### Application

<img width="550" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/5745f0ef-fd34-4b39-9115-1a4dbed5b3fc">

- 서버 간 비동기 통신, 이벤트 발행 보장 : [#45](https://github.com/ecsimsw/pic-up/issues/45)
- 비관적 락의 DB 커넥션 점유 문제 개선, 레디스 분산락 적용 : [#42](https://github.com/ecsimsw/pic-up/issues/42)
- 파일 삭제 이벤트 원자성 보장, 더미 파일 제거 Cron job : [#31](https://github.com/ecsimsw/pic-up/issues/31)     
- 통합테스트 외부 환경 의존 제거, 테스트 멱등성과 @Transactional : [#30](https://github.com/ecsimsw/pic-up/issues/30)     
- CDN 개인 파일 URL 암호화, 허용 IP, 유효 기간 지정과 URL 캐싱 : [#40](https://github.com/ecsimsw/pic-up/issues/40)
- Dropzone, Raw binary data 전송 : [#29](https://github.com/ecsimsw/pic-up/issues/29)
- 파일 업로드 속도 개선, 클라이언트 S3 직접 업로드와 썸네일 생성 Lambda 제작 : [#24](https://github.com/ecsimsw/pic-up/issues/24)     

### Infrastructure

<img width="550" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/eebda44e-7555-4425-906e-a135eda905fb">

- 배포 시점에 Vault secret key 주입, Vault kubernetes injector : [#38](https://github.com/ecsimsw/pic-up/issues/38)
- k8s Rolling update 무중단 배포, 내장 톰캣 Graceful shutdown 동작 원리 : [#37](https://github.com/ecsimsw/pic-up/issues/37)     
- 부하 테스트로 배포 리소스 크기 결정 : [#36](https://github.com/ecsimsw/pic-up/issues/36)
- DB 쿼리 성능 개선, 인덱스 튜닝과 커서 기반 페이지네이션 전환 : [#35](https://github.com/ecsimsw/pic-up/issues/35)
- 이미지 파일 업로드 부하테스트, 응답 시간과 메모리 변화 확인 : [#19](https://github.com/ecsimsw/pic-up/issues/19)     

### Stacks
- Framework : Java 17, Spring boot 2.7, Hibernate, JUnit5, Mokito
- BE tools : Mysql, Redis, RabbitMQ, H2, Flyway
- BE libray : S3Mock, Embedded Redis, Thumbnailator, Jcodec
- FE library : Dropzone, lightGallery, video-js
- On premise : Nginx, Kubernetes, Vault, NFS, Vagrant
- Cloud : S3, Cloudfront, Route53, Lambda, Terraform
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
