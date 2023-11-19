# Pic up
저희 부모님은 클라우드 사용을 어려워 하십니다.    
아직도 어렸을 때 인화해 둔 아날로그 식 앨범만을 좋아하십니다.          
부모님께서 쉽게 사용하실 수 있는 사진 스토리지를 만들고 있습니다.     

### Skills
- Backend : Java, Spring boot, JPA, RabbitMQ, MongoDB, Mysql, Redis, CriteriaAPI, JWT, AES256/SHA256
- Dev/Test env : JUnit5, Docker, Guava, Lombok, H2, Embed mongo, S3mock
- Infrastructure : Kubernetes, Terraform, NFS, Kong gateway, Cert-manager, MetalLB, EC2, S3, Vagrant
- CI/CD : JIB, Github actions, GHCR

### Application

- Architecture : [Picup 구조 소개하기 / 애플리케이션 구조, 배포 구조, CI/CD 플로우](https://ecsimsw.tistory.com/entry/Picup-%EC%9D%98-%EC%A0%84%EC%B2%B4-%EC%9D%B8%ED%94%84%EB%9D%BC-%EA%B5%AC%EC%A1%B0-%EC%86%8C%EA%B0%9C%ED%95%98%EA%B8%B0-%EB%84%A4%ED%8A%B8%EC%9B%8C%ED%81%AC-%EA%B5%AC%EC%A1%B0%EB%B6%80%ED%84%B0-CICD-%EA%B9%8C%EC%A7%80)
- Message queue : [메시지 큐 사용 이유와 RabbitMQ 주요 옵션, 재앙 시나리오 소개](https://ecsimsw.tistory.com/entry/%EB%A9%94%EC%8B%9C%EC%A7%80-%ED%81%90-%EC%82%AC%EC%9A%A9-%EC%9D%B4%EC%9C%A0%EC%99%80-RabbitMQ-%EC%A3%BC%EC%9A%94%E2%80%93%EC%98%B5%EC%85%98-%EC%9E%AC%EC%95%99-%EC%8B%9C%EB%82%98%EB%A6%AC%EC%98%A4-%EC%86%8C%EA%B0%9C)
- Cursor based pagination : [인덱스 기반에서 커서 기반 페이지네이션으로 / 더미 데이터 준비와 쿼리 테스트](https://ecsimsw.tistory.com/entry/%EC%BB%A4%EC%84%9C-%EA%B8%B0%EB%B0%98-%ED%8E%98%EC%9D%B4%EC%A7%80%EB%84%A4%EC%9D%B4%EC%85%98-%EB%8D%94%EB%AF%B8-%EB%8D%B0%EC%9D%B4%ED%84%B0-%EC%A4%80%EB%B9%84%EC%99%80-%EC%BF%BC%EB%A6%AC-%ED%85%8C%EC%8A%A4%ED%8A%B8)
- DB replication : [Mysql DB Replication 으로 데이터 백업, 쿼리 분산](https://ecsimsw.tistory.com/entry/Mysql-DB-Replication-%EC%9C%BC%EB%A1%9C-%EB%8D%B0%EC%9D%B4%ED%84%B0-%EB%B0%B1%EC%97%85-%EC%BF%BC%EB%A6%AC-%EB%B6%84%EC%82%B0)
- MSR for data backup : [Mysql DB Multi source replication 으로 여러 소스 데이터 백업](https://www.blog.ecsimsw.com/entry/Mysql-DB-Multi-source-replication-%EC%9C%BC%EB%A1%9C-%EB%B0%B1%EC%97%85-%EB%A1%9C%EA%B7%B8-%EB%8D%B0%EC%9D%B4%ED%84%B0-%EC%A4%91%EC%95%99%ED%99%94)
- File CRUD scenario : [Picup 에서 사진 파일을 다루는 방법, AWS S3 대신 Vultr object storage](https://ecsimsw.tistory.com/entry/Vultr-S3-%EC%99%80-%EC%9D%B4%EB%AF%B8%EC%A7%80-%EB%B0%B1%EC%97%85-%EC%A0%95%EC%B1%85)
- Data cache with Redis
- Retry, recover strategy, Dead letter queue
- Jwt auth flow / Access token, Refresh token
- Encryption, Decryption
- Test with JUnit5, Mockito, Embedded DB

<img width="1263" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/b60fc9b9-5bfa-4502-8013-7a1aea59772e">

### Infrastructure
- Setting Up an On-premise Kubernetes : [Infra-docs](https://github.com/ecsimsw/pic-up/tree/main/infra-docs)
- Kubernetes configuration : [Kubernetes configuration](https://github.com/ecsimsw/pic-up/tree/main/infra-kubernetes)
- Code as cloud server with terraform : [Infra - Cloud server](https://github.com/ecsimsw/pic-up/tree/main/infra-terraform)
- NFS for file main storage
- Object storage for file backup storage
- HPA and stress test
  
<img width="1386" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/3e1517db-9823-48b1-8eea-4828ec856df1">

</br></br>

### CI/CD

- Released container images : [GHCR-picup](https://github.com/ecsimsw?tab=packages&tab=packages&q=picup)
- Gradle cache
- JIB
 
<img width="1299" alt="image" src="https://github.com/ecsimsw/pic-up/assets/46060746/2fc85f4c-9f20-4142-bdc5-b2ecb72c7092">

</br></br>

### 이번 스프린트
- [x] Cache first page
- [x] Set NFS volume for main image storage
- [x] S3 mocking for dev, test
- [ ] Gateway rate limiting
- [ ] Configure monitoring system
