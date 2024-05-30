## Application

#### 개발 환경 구성
```
docker-compose -f docker-compose-dev.yaml up -d
```
#### Container image 빌드, GHCR 업로드
```
./buildScript.sh $PROJECT_NAME $SPRING_PROFILE

ex)
./buildScript.sh storage-api prod
./buildScript.sh storage-batch prod
./buildScript.sh member-api prod
```

#### RabbitMQ exchange, queue, binding 초기화
```
chmod +x message-queue-init.sh
./message-queue-init.sh
```

#### 배포 체크 리스트
```
1. Redis
2. RabbitMQ, Init queues
3. Vault or Environment variables
4. CloudFront private_key.pem
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
