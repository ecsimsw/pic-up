# Pic up
저희 부모님은 클라우드 스토리지 사용을 어려워 하십니다.    
아직도 어렸을 때 인화해 둔 아날로그 식 앨범을 더 좋아하세요.        
부모님께서 쉽게 사용하실 수 있는 사진 스토리지를 만들고 있습니다.     

### Skills
- Backend : Java, Spring boot, JPA, RabbitMQ, MongoDB, Mysql, Redis, CriteriaAPI, JWT, CompletableFuture, AES256/SHA256
- Dev, Test : JUnit5, Docker, Guava, Lombok, H2, Embedded mongo, S3mock
- Infrastructure : Kubernetes, Terraform, NFS, Kong gateway, Nginx, Cert-manager, MetalLB, EC2, S3, Vagrant
- CI/CD : JIB, Github actions, GHCR

### Application

- Async / Non blocking image upload : [이미지 비동기 업로드, 논 블록킹 조합 / 예외 처리와 더미 파일](https://ecsimsw.tistory.com/entry/%EC%9D%B4%EB%AF%B8%EC%A7%80-%EC%97%85%EB%A1%9C%EB%93%9C-%EB%B9%84%EB%8F%99%EA%B8%B0-%EC%B2%98%EB%A6%AC-%ED%9B%84-%EA%B2%B0%EA%B3%BC-%EC%A1%B0%ED%95%A9)
- Architecture : [Picup 구조 소개하기 / 애플리케이션 구조, 배포 구조, CI/CD 플로우](https://ecsimsw.tistory.com/entry/Picup-%EC%9D%98-%EC%A0%84%EC%B2%B4-%EC%9D%B8%ED%94%84%EB%9D%BC-%EA%B5%AC%EC%A1%B0-%EC%86%8C%EA%B0%9C%ED%95%98%EA%B8%B0-%EB%84%A4%ED%8A%B8%EC%9B%8C%ED%81%AC-%EA%B5%AC%EC%A1%B0%EB%B6%80%ED%84%B0-CICD-%EA%B9%8C%EC%A7%80)
- Message queue : [메시지 큐 사용 이유와 RabbitMQ 주요 옵션, 재앙 시나리오 소개](https://ecsimsw.tistory.com/entry/%EB%A9%94%EC%8B%9C%EC%A7%80-%ED%81%90-%EC%82%AC%EC%9A%A9-%EC%9D%B4%EC%9C%A0%EC%99%80-RabbitMQ-%EC%A3%BC%EC%9A%94%E2%80%93%EC%98%B5%EC%85%98-%EC%9E%AC%EC%95%99-%EC%8B%9C%EB%82%98%EB%A6%AC%EC%98%A4-%EC%86%8C%EA%B0%9C)
- Cursor based pagination : [인덱스 기반에서 커서 기반 페이지네이션으로 / 더미 데이터 준비와 쿼리 테스트](https://ecsimsw.tistory.com/entry/%EC%BB%A4%EC%84%9C-%EA%B8%B0%EB%B0%98-%ED%8E%98%EC%9D%B4%EC%A7%80%EB%84%A4%EC%9D%B4%EC%85%98-%EB%8D%94%EB%AF%B8-%EB%8D%B0%EC%9D%B4%ED%84%B0-%EC%A4%80%EB%B9%84%EC%99%80-%EC%BF%BC%EB%A6%AC-%ED%85%8C%EC%8A%A4%ED%8A%B8)
- DB replication : [Mysql DB Replication 으로 데이터 백업, 쿼리 분산](https://ecsimsw.tistory.com/entry/Mysql-DB-Replication-%EC%9C%BC%EB%A1%9C-%EB%8D%B0%EC%9D%B4%ED%84%B0-%EB%B0%B1%EC%97%85-%EC%BF%BC%EB%A6%AC-%EB%B6%84%EC%82%B0)
- MSR for data backup : [Mysql DB Multi source replication 으로 여러 소스 데이터 백업](https://www.blog.ecsimsw.com/entry/Mysql-DB-Multi-source-replication-%EC%9C%BC%EB%A1%9C-%EB%B0%B1%EC%97%85-%EB%A1%9C%EA%B7%B8-%EB%8D%B0%EC%9D%B4%ED%84%B0-%EC%A4%91%EC%95%99%ED%99%94)
- Data cache with Redis : [Picup의 캐시 사용 전략, 레디스 주요 옵션 정리](https://www.blog.ecsimsw.com/entry/%EC%BA%90%EC%8B%9C%EB%A1%9C-%EC%A1%B0%ED%9A%8C-%EC%84%B1%EB%8A%A5-%EA%B0%9C%EC%84%A0-%EB%A0%88%EB%94%94%EC%8A%A4-%EC%BA%90%EC%8B%9C-%EC%82%AC%EC%9A%A9-%EC%9D%B4%EC%9C%A0%EC%99%80-%EC%A0%84%EB%9E%B5)
- File CRUD scenario : [Picup 에서 사진 파일을 다루는 방법, AWS S3 대신 Vultr object storage](https://ecsimsw.tistory.com/entry/Vultr-S3-%EC%99%80-%EC%9D%B4%EB%AF%B8%EC%A7%80-%EB%B0%B1%EC%97%85-%EC%A0%95%EC%B1%85)
- Jwt auth flow / library : [simple-auth / 토큰 인증 시나리오를 대신해주는 라이브러리](https://ecsimsw.tistory.com/entry/simple-auth-%ED%86%A0%ED%81%B0-%EC%9D%B8%EC%A6%9D-%EC%8B%9C%EB%82%98%EB%A6%AC%EC%98%A4%EB%A5%BC-%EB%8C%80%EC%8B%A0%ED%95%B4%EC%A3%BC%EB%8A%94-%EB%9D%BC%EC%9D%B4%EB%B8%8C%EB%9F%AC%EB%A6%AC)
- Retry, recover strategy, Dead letter queue
- Encryption, Decryption
- Test with JUnit5, Mockito, Embedded DB

![image](https://github.com/ecsimsw/pic-up/assets/46060746/228b23f4-c303-44af-96cb-7b7d84557943)


### Infrastructure
- Setting Up an On-premise Kubernetes : [Infra-docs](https://github.com/ecsimsw/pic-up/tree/main/infra-docs)
- Kubernetes configuration : [Kubernetes configuration](https://github.com/ecsimsw/pic-up/tree/main/infra-kubernetes)
- Code as cloud server with terraform : [Infra - Cloud server](https://github.com/ecsimsw/pic-up/tree/main/infra-terraform)
- NFS for file main storage
- Object storage for file backup storage
- HPA and stress test
  
![image](https://github.com/ecsimsw/pic-up/assets/46060746/6e16c4a5-2480-477c-84d8-c333e7aa523f)

<br>

### CI/CD

- Released container images : [GHCR-picup](https://github.com/ecsimsw?tab=packages&tab=packages&q=picup)
- Gradle cache
- JIB
<img width="1299" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/2fc85f4c-9f20-4142-bdc5-b2ecb72c7092">


### 다음 스프린트
- [x] Auth scenario library로 분리하기
- [x] S3 mocking for dev, test
- [x] Main storage, Backup storage 업로드 동시 처리
- [x] Health check k8s probes
- [ ] Configure monitoring system
