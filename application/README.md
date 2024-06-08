## Application

#### 모듈 구조
- auth : 로그인 토큰 인증 편의를 위한 Interceptor, Access/Refresh token 발급 서비스, JWT 토큰 유틸 
- common : 서버 간 통신 규약 (DTO), 사용자 정보 DB 암호화 유틸 
- logging : Http access log, Slow response alert, 로깅 편의를 위한 커스텀 유틸 
- member-api : 회원 관리 서비스 Rest API  
- member-core : 회원 관리 서비스 
- storage-api : 사용자 파일 기록, 스토리지 공간 관리, CDN 암호화 Rest API  
- storage-batch : 파일 스토리지 삭제 처리 Job 
- storage-core : 사용자 파일 기록, 스토리지 공간 관리 서비스

#### 개발 환경 구성
- Dev envs : Redis, RabbitMQ, H2
```
docker-compose -f docker-compose-dev.yaml up -d
```

#### 테스트 코드
- Test envs : H2, Embedded Redis, Mocked RabbitMQ / Mockito, JUnit5
```
./gradlew test
```

#### Project version
```
./gradlew -q printRootVersion
```

#### 빌드 스크립트
- Gradle build, Container image build, Upload to image registry  
```
./buildScript.sh $PROJECT_NAME $SPRING_PROFILE

ex)
./buildScript.sh storage-api prod
./buildScript.sh storage-batch prod
./buildScript.sh member-api prod
```

#### RabbitMQ 설정
- exchange, queue, binding 초기화 스크립트
```
chmod +x message-queue-init.sh
./message-queue-init.sh
```

#### 배포 체크 리스트
```
1. Redis
2. RabbitMQ, Init queues
3. Mysql, Flyway
4. Vault or Environment variables
5. CloudFront private_key.pem
```

#### Sample deployment

``` yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: storage-api
  namespace: picup
spec:
  selector:
    matchLabels:
      app: storage-api
  replicas: 1
  template:
    metadata:
      labels:
        app: storage-api
      annotations:
        vault.hashicorp.com/role: internal-app
        vault.hashicorp.com/agent-inject: "true"
        vault.hashicorp.com/agent-pre-populate-only : "true"  # disable sidecar
        vault.hashicorp.com/agent-inject-secret-picup: picup/common
        vault.hashicorp.com/agent-inject-template-picup: |
          {{- with secret "picup/common" -}}
          {{- range $k, $v := .Data }}
          export {{ $k }}={{ $v }}
          {{end}}{{end}}
    spec:
      containers:
        - name: storage-api
          image: ghcr.io/ecsimsw/picup/storage-api:latest
          imagePullPolicy: Always
          resources:
            requests:
              memory: "400Mi"
            limits:
              memory: "400Mi"
          volumeMounts:
            - name: picup-data
              mountPath: /ext-data      # READ private_key.pem
          ports:
            - containerPort: 8084
          startupProbe:
            httpGet:
              path: /actuator/health
              port: 8084
            failureThreshold: 100
            periodSeconds: 2
          readinessProbe:
            httpGet:
              path: /actuator/health
              port: 8084
            failureThreshold: 2
            periodSeconds: 100
          livenessProbe:
            httpGet:
              path: /actuator/health
              port: 8084
            failureThreshold: 2
            periodSeconds: 100
          lifecycle:
            preStop:
              exec:
                command: [ "sh", "-c", "sleep 10" ]    # Ensure that the IpTable is updated before kubelet kill container.
      volumes:
        - name: picup-data
          persistentVolumeClaim:
            claimName: storage-pvc
```
