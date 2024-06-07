## 모니터링
- Grafana
- Proemtheus
- Loki
- Nginxlog-prometheus-exporter

## 1. Nginxlog-prometheus-exporter
- Nginx 의 요청 처리량, 응답 시간을 모니터링하고 싶었다.
- Nginx 공식 prometheus exporter는 제공하는 메트릭은 충분하지 않았다.
- 응답 시간을 포함한 원했던 메트릭은 대부분 Nginx plus 에서만 제공한다.

```
http {
    log_format  main  '$remote_addr - $remote_user [$time_local] "$request" '
                      '$status $body_bytes_sent "$request_time" "$http_referer" '
                      '"$http_user_agent" "$http_x_forwarded_for"';

    access_log         /var/log/nginx/access.log  main;
    keepalive_timeout  65;

    include            /etc/nginx/conf.d/route-blog.conf;
}
```
- 로그 포맷에 $request_time 을 추가하면 응답 시간은 쉽게 Log 파일에 기록할 수 있다.
- request_time 은 클라이언트 요청의 첫 바이트를 읽는 시점부터, 응답의 마지막 바이트를 전달하는 시점까지의 시간을 초로 기록한다.
- Nginxlog-prometheus-exporter는 Nginx의 로그 파일을 파싱하여 Prometheus exporter로 메트릭 값을 응답한다.
- REF, `https://github.com/martin-helmich/prometheus-nginxlog-exporter`

### Log exporter config 
- namespace 의 name 을 Prefix 로 프로메테우스 메트릭 이름이 생성된다.
- Exporter에서 읽을 Log 파일 위치를 지정한다.
```
listen:
  port: 4040
  address: "0.0.0.0"
  metrics_endpoint: "/metrics"

consul:
  enable: false

namespaces:
  - name: nginx
    format: "$remote_addr - $remote_user [$time_local] \"$request\" $status $body_bytes_sent \"$request_time\" \"$http_referer\" \"$http_user_agent\" \"$http_x_forwarded_for\""
    source:
      files:
        - /var/log/nginx/access.log
```
 
### Run log exporter
```
version: '3'
services:
  nginx-log-exporter:
    image: ghcr.io/martin-helmich/prometheus-nginxlog-exporter/exporter:v1.11-amd64
    volumes:
      - ~/dev/data/nginx:/var/log/nginx
      - ./exporter/prometheus-nginxlog-exporter.yaml:/config/prometheus-nginxlog-exporter.yaml
    command:
      - -config-file
      - /config/prometheus-nginxlog-exporter.yaml
    ports:
      - "4040:4040"
    extra_hosts:
      - "host.docker.internal:host-gateway"
```

#### Grafana, Prometheus
- Prometheus에서 Log exporter를 Scrap 경로로 추가한다.
```
scrape_configs:
  - job_name: 'nginxlog-exporter'
    static_configs:
      - targets: [ "${exporter_ip}:4040" ]
```
- 아래는 제작자가 추천하는 Grafana query 이다.
```
Average response time
: sum(rate(nginx_http_response_time_seconds_sum[5m])) by (instance) / sum(rate(nginx_http_response_time_seconds_count[5m])) by (instance)

Requests per second
: sum(rate(nginx_http_response_time_seconds_count[1m])) by (instance)

Response time (90% quantile)
: nginx_http_response_time_seconds{quantile="0.9",method="GET",status=~"2[0-9]*"}

Status codes per second
: sum(rate(nginx_http_response_size_bytes[5m])) by (instance)
```

### 적용 결과
- 대시보드에 표시하고 부하테스트를 진행하여 결과를 확인한다.
- 좌측 : Spring actuator의 prometheus exporter
- 우측 : Nginx log exporter
  
![image](https://github.com/ecsimsw/pic-up/assets/46060746/24cd861b-0410-44ae-be28-6a78b124030f)

</br>

## 2. 부하테스트와 모니터링으로 리소스 크기 결정하기

### 테스트 목표 
- 더미데이터로 유저 3백개, 앨범 3천개, 사진 3천만개를 생성한다.
- 리소스 사용량을 확인하고 파드 수, 리소스 사이즈를 결정한다.
- Container, Heap memory 사용률이 최대 80%를 넘지 않도록 한다.

### 목표 수용치 (vUser : 300, Duration : 10m)
1. My info : 서로 다른 유저가 로그인, 사용자 정보 조회 
2. Upload image : 서로 다른 유저가 이미지 업로드 Url 요청 
3. Upload image : 한 유저가 동시에 100개의 이미지 업로드 Url 요청 
4. Commit image : 서로 다른 유저가 이미지 업로드 Commit 
5. Commit image : 한 유저가 동시에 100개의 이미지 업로드 Commit 
6. Get pictures : 서로 다른 유저가 무작위 페이지 Picture 조회 반복

### 최소 메모리 확인
- 실행에 필요한 최소 Container 메모리 : 242Mi
- 테스트 통과에 필요한 최소 Container 메모리 : 450Mi

### 1 pod, 500Mi
![image](https://github.com/ecsimsw/pic-up/assets/46060746/eab4c571-f672-42b8-8301-00aacc70fee6)
- 테스트는 통과하나 Heap 최대 사용량 : 93% -> Java OOM 위험

### 3 pod, 400Mi
![image](https://github.com/ecsimsw/pic-up/assets/46060746/9ec16999-1cb8-4e7d-a940-83ddaee5f9ca)
- Heap 최대 사용량은 83% -> 메모리 사용량은 만족
- 컨테이너가 사용하는 메모리량이 평균 340Mi, limit 에 최대 88% 사용 -> Pod OOM killed 위험, 노드 메모리 과할당

### 2 pod, 500Mi
![image](https://github.com/ecsimsw/pic-up/assets/46060746/3700b519-f348-495f-b7ff-829ceb043fbf)
- Heap 최대 사용량 85% -> 메모리 사용량 만족
- 컨테이너가 사용하는 메모리량이 평균 340Mi, limit 에 최대 67% 사용

![image](https://github.com/ecsimsw/pic-up/assets/46060746/5906001e-972f-4a6d-9be8-5f2d4db99096)

- 요청이 몰리는 상황을 가정하여, 2분동안 500명의 가상 유저, 약 9만개의 요청을 전달

![image](https://github.com/ecsimsw/pic-up/assets/46060746/3f9b9292-b5b9-43b9-a3c1-c50672fa2cdd)

- 결정!

### 인프라
- vCpu 2, vMem 2GB -> vCpu 2, vMem 4GB
- cpu 사용량이 널널해서 코어를 1로 줄이려고 했는데, master node cpu 최소 사양이 cpu 2 이상.
- 억지로 에러 무시하고 kubeadm init 해도 coreDns cpu request가 2이어서 번거로움
- cpu 는 그대로 2 코어를 두는 것으로 😅
- 아래는 결정한 Picup Pod (mem 500Mi)를 2개 띄웠을 때의 htop, 메모리 42% 사용

![image](https://github.com/ecsimsw/pic-up/assets/46060746/266f2998-32f9-4002-9cf5-656d444aaa82)

</br>

## 3. 브라우저 waterfall / Http 2.0 

### Http 1.1 / HOL
- 브라우저에서 배포한 메인 페이지를 호출하면 아래와 같은 waterfall 을 갖는다.
- 맨 위의 html 파일을 응답받는데 오래 걸리고, 그 이후 js, css 파일 요청-응답, js 로 페이지를 그릴 때 필요한 api 요청들이 이후에 처리되게 된다.  

![image](https://github.com/ecsimsw/pic-up/assets/46060746/b922a814-f54b-445a-9fc6-074525d7f281)

- 여기서 회색은 브라우저의 커넥션 대기, 노란색은 TCP 커넥션을 위한 3way handshake 로 그 시간은 당장 내가 튜닝할 거리 밖이라 판단했다.
- http/1.1 사용으로 한 커넥션 안에서 여러 요청 (index.js, index.css) 를 처리하여 handshake 를 줄일 수 있었지만,
- 여전히 HOL blocking 문제로 index.js 의 응답 시간에 따라 index.css 요청에도 지연이 발생하고 있다.
- HOL을 조금이나마 해결하기 위해 추가 커넥션을 더 수립하고 있는데, Handshake 에 시간이 필요할뿐더러 브라우저가 동시에 수립할 수 있는 커넥션 개수는 6개로 한정되어 있다.

### Http 2.0 / Multiplexing
- 아래는 HTTP 2.0 을 적용한 이후 같은 요청에 대한 waterfall 이다.
- TCP 커넥션은 한번만 일어나 handshake 비용과 커넥션 사용을 줄이고, index.js 와 index.css 에서 HOL bloking 문제가 개선된 것을 볼 수 있다.
- HTTP 2.0 에서 헤더 압축과 중복 제거로 전체적인 파일 응답 사이즈가 줄었다.
- (index.html, index.js은 이후에 코드가 추가되어 사이즈가 늘었다)

![image](https://github.com/ecsimsw/pic-up/assets/46060746/b18b01f8-febe-4667-b8d3-bf22066903b6)

### server push

- Http 2.0 의 Server push 로 index.html 파일과 필요한 js, css 를 함께 전달하는 것을 고려했다.
- Nginx, Chrome에서 Server push 로 오히려 성능 저하가 발생하여 더 이상 http 2.0의 server push 를 지원하지 않는다.
- REF, chrome : https://developer.chrome.com/blog/removing-push?hl=ko
- REF, nginx : https://nginx.org/en/docs/http/ngx_http_v2_module.html


