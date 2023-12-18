## Monitoring

1. Api server (Spring boot actuator, micrometer-prometheus)
```   
- 메모리 사용량
- Cpu 사용량
- 분당 request 수, 서버 에러 응답 수
- 응답 시간 (sending + waiting + receiving)
- 스레드 수와 상태
- GC 수행 시간과 횟수
- Eden space, Survivor space, Tenured space 사이즈
``` 

2. k8s cluster (kube-state-metic, node-exporter, Kubelet)
```
- 전체 노드 개수 / Unavailable 노드 수
- Replica 수 / Unavailable replica 수
- 실행 중인 Pod 수
- 실행 중인 Container 수
- Pod Cpu, Memory 사용률
- Pod 네트워크 송수신량
- Evicted 이벤트 수
- Pending, Unkown 상태의 pod 수
``` 

3. Prod server, docker containers (cAdvisor)
```
- 전체 컨테이너 개수
- 전체 컨테이너 메모리 사용률
- 전체 컨테이너 CPU 사용량
- 각 컨테이너별 메모리, CPU 사용률
- 각 컨테이너별 네트워크 송수신량
```
