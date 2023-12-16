# Pic up
저희 부모님은 클라우드 스토리지 사용을 어려워 하십니다.    
아직도 어렸을 때 인화해 둔 아날로그 식 앨범을 더 좋아하세요.        
부모님께서 쉽게 사용하실 수 있는 사진 스토리지를 만들고 있습니다.     

### Development stacks
- Backend : Java, Spring boot, JPA, RabbitMQ, Criteria, CompletableFuture, MongoDB, Mysql, Redis, JWT, AES256/SHA256
- Dev env : Guava, JUnit5, Mockito, S3mock, Lombok, H2, Embedded mongo, JDK11
- Infrastructure : Kubernetes, Terraform, Nginx, NFS, Kong, Docker, EC2, S3, Vagrant
- Monitoring, Test : Prometheus, Grafana, K6
- CI/CD : JIB, Github actions, GHCR

### Application

- 비동기 처리, 논블록킹 조합으로 이미지 다중 소스 업로드 : [docs](https://ecsimsw.tistory.com/entry/%EC%9D%B4%EB%AF%B8%EC%A7%80-%EC%97%85%EB%A1%9C%EB%93%9C-%EB%B9%84%EB%8F%99%EA%B8%B0-%EC%B2%98%EB%A6%AC-%ED%9B%84-%EA%B2%B0%EA%B3%BC-%EC%A1%B0%ED%95%A9)
- 메시지 큐를 이용한 서버간 비동기 통신, 재시도 정책과 복구 처리 : [docs](https://ecsimsw.tistory.com/entry/%EB%A9%94%EC%8B%9C%EC%A7%80-%ED%81%90-%EC%82%AC%EC%9A%A9-%EC%9D%B4%EC%9C%A0%EC%99%80-RabbitMQ-%EC%A3%BC%EC%9A%94%E2%80%93%EC%98%B5%EC%85%98-%EC%9E%AC%EC%95%99-%EC%8B%9C%EB%82%98%EB%A6%AC%EC%98%A4-%EC%86%8C%EA%B0%9C)
- 인덱스 기반에서 커서 기반의 페이지 네이션 전환으로 조회 성능 개선 : [docs](https://ecsimsw.tistory.com/entry/%EC%BB%A4%EC%84%9C-%EA%B8%B0%EB%B0%98-%ED%8E%98%EC%9D%B4%EC%A7%80%EB%84%A4%EC%9D%B4%EC%85%98-%EB%8D%94%EB%AF%B8-%EB%8D%B0%EC%9D%B4%ED%84%B0-%EC%A4%80%EB%B9%84%EC%99%80-%EC%BF%BC%EB%A6%AC-%ED%85%8C%EC%8A%A4%ED%8A%B8)
- DB 레플리케이션으로 백업과 부하분산 : [docs](https://ecsimsw.tistory.com/entry/Mysql-DB-Replication-%EC%9C%BC%EB%A1%9C-%EB%8D%B0%EC%9D%B4%ED%84%B0-%EB%B0%B1%EC%97%85-%EC%BF%BC%EB%A6%AC-%EB%B6%84%EC%82%B0)
- MSR 으로 백업 DB 중앙화 : [docs](https://www.blog.ecsimsw.com/entry/Mysql-DB-Multi-source-replication-%EC%9C%BC%EB%A1%9C-%EB%B0%B1%EC%97%85-%EB%A1%9C%EA%B7%B8-%EB%8D%B0%EC%9D%B4%ED%84%B0-%EC%A4%91%EC%95%99%ED%99%94)
- 캐시를 이용한 조회 성능 개선 : [docs](https://www.blog.ecsimsw.com/entry/%EC%BA%90%EC%8B%9C%EB%A1%9C-%EC%A1%B0%ED%9A%8C-%EC%84%B1%EB%8A%A5-%EA%B0%9C%EC%84%A0-%EB%A0%88%EB%94%94%EC%8A%A4-%EC%BA%90%EC%8B%9C-%EC%82%AC%EC%9A%A9-%EC%9D%B4%EC%9C%A0%EC%99%80-%EC%A0%84%EB%9E%B5)
- JWT Access token, Refresh token 사용자 인증. 관련 라이브러 제작과 배포 : [code](https://github.com/ecsimsw/simple-auth)
- 사용자 정보 암호화

<img width="1384" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/7e2016c9-c4cc-44d9-b5cc-67de3c3bf9b8">


### Infrastructure
- k8s, JVM, 도커 컨테이너 모니터링과 부하 테스트 : [docs](https://www.blog.ecsimsw.com/entry/JVM-%EB%AA%A8%EB%8B%88%ED%84%B0%EB%A7%81-%ED%94%84%EB%A1%9C%EB%A9%94%ED%85%8C%EC%9A%B0%EC%8A%A4%EC%99%80-JVM-%EB%A9%94%EB%AA%A8%EB%A6%AC-%ED%8A%9C%EB%8B%9D)
- Kubernetes 배포와 HPA, MSA 구조로 유연한 스케일 아웃
 : [docs](https://ecsimsw.tistory.com/entry/Picup-%EC%9D%98-%EC%A0%84%EC%B2%B4-%EC%9D%B8%ED%94%84%EB%9D%BC-%EA%B5%AC%EC%A1%B0-%EC%86%8C%EA%B0%9C%ED%95%98%EA%B8%B0-%EB%84%A4%ED%8A%B8%EC%9B%8C%ED%81%AC-%EA%B5%AC%EC%A1%B0%EB%B6%80%ED%84%B0-CICD-%EA%B9%8C%EC%A7%80) / [config](https://github.com/ecsimsw/pic-up/tree/main/infra-docs)
- 테라폼, AWS resource IaC : [code](https://github.com/ecsimsw/pic-up/tree/main/infra-terraform)
- NFS, 다중 서버에서 디스크 파일 중앙화 : [docs](https://github.com/ecsimsw/pic-up/blob/main/infra-docs/11_nfs.md)
  
<img width="1372" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/a47932aa-4f55-47d9-bd82-605e0f663f24">

<br>

### Network

<img width="1420" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/3b50f337-8dae-47c8-a63f-e8dc11835935">



<br>

### CI/CD

- Released container images : [GHCR-picup](https://github.com/ecsimsw?tab=packages&tab=packages&q=picup)
- Github actions
- Gradle cache
- JIB

<img width="1299" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/2fc85f4c-9f20-4142-bdc5-b2ecb72c7092">

### 다음 스프린트
- [x] 서버 애플리케이션 Rate limit
- [ ] 동시성 문제 확인
- [ ] Log 모니터링

### 지난 스프린트
- [x] Auth scenario library로 분리하기
- [x] S3 mocking for dev, test
- [x] Main storage, Backup storage 업로드 동시 처리
- [x] Health check k8s probes
- [x] Configure monitoring system
