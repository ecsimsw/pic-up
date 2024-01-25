# Pic-up
저희 부모님은 클라우드 스토리지 사용을 어려워 하십니다.    
아직도 어렸을 때 인화해 둔 아날로그 식 앨범을 더 좋아하세요.        
부모님께서 쉽게 사용하실 수 있는 사진 스토리지를 만들고 있습니다.     

## Docs

#### 분산 환경에서 안전한 데이터 처리
- Transactional outbox pattern, 메시지와 DB 원자성 보장 : [docs](https://ecsimsw.tistory.com/entry/Transactional-outbox-pattern-%EC%9C%BC%EB%A1%9C), [code](https://github.com/ecsimsw/pic-up/blob/main/server-api/api-album/src/main/java/ecsimsw/picup/service/ImageEventOutboxService.java)
- 이미지 다중 소스 동시 업로드와 결과 조합 : [docs](https://ecsimsw.tistory.com/entry/%EC%9D%B4%EB%AF%B8%EC%A7%80-%EC%97%85%EB%A1%9C%EB%93%9C-%EB%B9%84%EB%8F%99%EA%B8%B0-%EC%B2%98%EB%A6%AC-%ED%9B%84-%EA%B2%B0%EA%B3%BC-%EC%A1%B0%ED%95%A9), [code](https://github.com/ecsimsw/pic-up/blob/main/server-api/api-storage/src/main/java/ecsimsw/picup/service/StorageService.java#L49)
- RabbitMQ, 서버간 비동기 통신, 재시도 정책과 복구 처리 : [docs](https://ecsimsw.tistory.com/entry/%EB%A9%94%EC%8B%9C%EC%A7%80-%ED%81%90-%EC%82%AC%EC%9A%A9-%EC%9D%B4%EC%9C%A0%EC%99%80-RabbitMQ-%EC%A3%BC%EC%9A%94%E2%80%93%EC%98%B5%EC%85%98-%EC%9E%AC%EC%95%99-%EC%8B%9C%EB%82%98%EB%A6%AC%EC%98%A4-%EC%86%8C%EA%B0%9C)

#### 동시성 문제 해결과 락
- DB 락의 커넥션 점유 문제 확인과 해결 : [docs](https://ecsimsw.tistory.com/entry/%EB%8F%99%EC%8B%9C%EC%84%B1-%EB%AC%B8%EC%A0%9C-%ED%95%B4%EA%B2%B0-DB-%EC%BB%A4%EB%84%A5%EC%85%98-%EC%A0%90%EC%9C%A0-%ED%99%95%EC%9D%B8%EA%B3%BC-%EC%84%B1%EB%8A%A5-%EA%B0%9C%EC%84%A0), [code](https://github.com/ecsimsw/pic-up/blob/main/server-api/api-album/src/main/java/ecsimsw/picup/service/AlbumService.java#L46)
- 동시성 문제 해결을 위한 락 : [docs](https://ecsimsw.tistory.com/entry/%EB%8F%99%EC%8B%9C%EC%84%B1-%ED%85%8C%EC%8A%A4%ED%8A%B8%EC%99%80-%ED%95%B4%EA%B2%B0-%EB%B0%A9%EC%95%88)
  
#### 데이터 백업과 DB 성능 개선
- 커서 기반 페이지 네이션 전환으로 조회 성능 개선 : [docs](https://ecsimsw.tistory.com/entry/%EC%BB%A4%EC%84%9C-%EA%B8%B0%EB%B0%98-%ED%8E%98%EC%9D%B4%EC%A7%80%EB%84%A4%EC%9D%B4%EC%85%98-%EB%8D%94%EB%AF%B8-%EB%8D%B0%EC%9D%B4%ED%84%B0-%EC%A4%80%EB%B9%84%EC%99%80-%EC%BF%BC%EB%A6%AC-%ED%85%8C%EC%8A%A4%ED%8A%B8), [code](https://github.com/ecsimsw/pic-up/blob/main/server-api/api-album/src/main/java/ecsimsw/picup/service/PictureService.java#L135)
- DataSource 헬스 체크와 동적 라우팅으로 DB 서버 다운 대비 : [docs](https://ecsimsw.tistory.com/entry/Dynamic-DataSource-%EB%9D%BC%EC%9A%B0%ED%8C%85%EC%9C%BC%EB%A1%9C-DB-%EC%84%9C%EB%B2%84-%EB%8B%A4%EC%9A%B4%EC%8B%9C-%EC%B2%98%EB%A6%AC), [code](https://github.com/ecsimsw/pic-up/blob/main/server-api/api-album/src/main/java/ecsimsw/picup/config/DataSourceHealth.java)
- DB 레플리케이션으로 백업과 부하분산 : [docs](https://ecsimsw.tistory.com/entry/Mysql-DB-Replication-%EC%9C%BC%EB%A1%9C-%EB%8D%B0%EC%9D%B4%ED%84%B0-%EB%B0%B1%EC%97%85-%EC%BF%BC%EB%A6%AC-%EB%B6%84%EC%82%B0)
- MSR 으로 백업 DB 중앙화 : [docs](https://www.blog.ecsimsw.com/entry/Mysql-DB-Multi-source-replication-%EC%9C%BC%EB%A1%9C-%EB%B0%B1%EC%97%85-%EB%A1%9C%EA%B7%B8-%EB%8D%B0%EC%9D%B4%ED%84%B0-%EC%A4%91%EC%95%99%ED%99%94)
- 캐시를 이용한 조회 성능 개선 : [docs](https://www.blog.ecsimsw.com/entry/%EC%BA%90%EC%8B%9C%EB%A1%9C-%EC%A1%B0%ED%9A%8C-%EC%84%B1%EB%8A%A5-%EA%B0%9C%EC%84%A0-%EB%A0%88%EB%94%94%EC%8A%A4-%EC%BA%90%EC%8B%9C-%EC%82%AC%EC%9A%A9-%EC%9D%B4%EC%9C%A0%EC%99%80-%EC%A0%84%EB%9E%B5), [code](https://github.com/ecsimsw/pic-up/blob/main/server-api/api-album/src/main/java/ecsimsw/picup/service/PictureService.java#L126)
- NFS, 다중 서버에서 디스크 파일 중앙화 : [docs](https://github.com/ecsimsw/pic-up/blob/main/infra-docs/11_nfs.md)

#### 배포와 모니터링
- 리버스 프록시, 요청 호출 수 제한과 접근 가능 IP 제한 : [docs](https://ecsimsw.tistory.com/entry/Nginx-%EC%9A%94%EC%B2%AD-%ED%98%B8%EC%B6%9C-%EC%88%98-%EC%A0%9C%ED%95%9C%EA%B3%BC-%EC%A0%91%EA%B7%BC-%EA%B0%80%EB%8A%A5-IP-%EC%A0%9C%ED%95%9C), [code](https://github.com/ecsimsw/pic-up/tree/main/infra-gateway/config)
- k8s, JVM, 도커 컨테이너 모니터링과 부하 테스트 : [docs](https://www.blog.ecsimsw.com/entry/JVM-%EB%AA%A8%EB%8B%88%ED%84%B0%EB%A7%81-%ED%94%84%EB%A1%9C%EB%A9%94%ED%85%8C%EC%9A%B0%EC%8A%A4%EC%99%80-JVM-%EB%A9%94%EB%AA%A8%EB%A6%AC-%ED%8A%9C%EB%8B%9D), [code](https://github.com/ecsimsw/pic-up/tree/main/utils-monitoring)
- Kubernetes 배포와 HPA, 유연한 스케일 아웃
 : [code](https://github.com/ecsimsw/pic-up/tree/main/infra-kubernetes)
- 테라폼, AWS resource IaC : [code](https://github.com/ecsimsw/pic-up/tree/main/infra-terraform)

## Architecture

#### Application
<img width="800" alt="image" src="https://github.com/ecsimsw/pic-up/blob/main/infra-docs/application-arch.png?raw=true">

#### Infrastructure  
<img width="800" alt="image" src="https://github.com/ecsimsw/pic-up/blob/main/infra-docs/infra-arch.png?raw=true">

</br>

## Scenario

<img width="800" alt="image" src="https://github.com/ecsimsw/pic-up/blob/main/infra-docs/user-scenario.png">

<br>

## Stacks
- Java, Spring boot, JPA, CriteriaAPI
- RabbitMQ, RestTemplate, FutureAPI
- Mysql, Redis, MongoDB, S3
- JUnit5, Mockito, H2, Embedded mongo, S3mock
- Kubernetes, Terraform, Nginx, NFS, Docker, Vagrant
- Prometheus, Grafana, K6
- JIB, Github actions, GHCR
