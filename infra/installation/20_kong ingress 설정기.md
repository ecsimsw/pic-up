# Install Kong ingress controller
version : v2.90
pre-needed : metallb

### Install kong with helm
```
helm repo add kong https://charts.konghq.com
helm repo update
helm install kong kong/ingress -n kong --create-namespace
```

```
curl -i $PROXY_IP
```

```
HTTP/1.1 404 Not Found
Date: Sat, 11 Nov 2023 22:56:10 GMT
Content-Type: application/json; charset=utf-8
Connection: keep-alive
Content-Length: 52
X-Kong-Response-Latency: 0
Server: kong/3.4.2

{
  "message":"no Route matched with those values"
}%
```

```
HOST=$(kubectl get svc --namespace kong kong-gateway-proxy -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
PORT=$(kubectl get svc --namespace kong kong-gateway-proxy -o jsonpath='{.spec.ports[0].port}')
export PROXY_IP=${HOST}:${PORT}
echo $PROXY_IP
```

### Add ingress class

```
echo "
apiVersion: networking.k8s.io/v1
kind: IngressClass
metadata:
  name: kong
spec:
  controller: ingress-controllers.konghq.com/kong
" | kubectl apply -f -
```

### Create ingress & test with echo

```
echo "
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
 name: echo
 annotations:
   konghq.com/strip-path: 'true'
spec:
 ingressClassName: kong
 rules:
 - host: kong.example
   http:
     paths:
     - path: /echo
       pathType: ImplementationSpecific
       backend:
         service:
           name: echo
           port:
             number: 1027
" | kubectl apply -f -
```

```
kubectl apply -f https://docs.konghq.com/assets/kubernetes-ingress-controller/examples/echo-service.yaml
```

```
curl -i -H 'Host:kong.example' $PROXY_IP/echo
```

```
HTTP/1.1 200 OK
Content-Type: text/plain; charset=utf-8
Content-Length: 136
Connection: keep-alive
Date: Sat, 11 Nov 2023 22:58:16 GMT
X-Kong-Upstream-Latency: 3
X-Kong-Proxy-Latency: 1
Via: kong/3.4.2

Welcome, you are connected to node worker1.
Running on Pod echo-965f7cf84-tmgdq.
In namespace default.
```

### kube port-forwarding

목표는 외부 사용자가 라우터를 타고 들어와 kubernetes 클러스터의 pod 에 도달하는 것이다.     
현재까진 pod 들은 service 를 통해 목적지를 갖고 있고, service 는 ingress 에 의해 도메인 or 요청 경로를 기반으로 요청이 전달된다.
그리고 ingress 는 LB type service proxy 로 외부 IP 를 갖게 된다. 이렇게 외부에서 ingress proxy service ip 또는 node ip:port 로 ingress 에 접속하고 요청이 넘어가게 되는 것이다.    
  
여기서 picup service 는 한단계가 더 추가되어야 한다. 앞서 ingress proxy service 가 갖는 외부 ip 가, router 에서 전달이 가능하다는 것을 확인해야 한다.   
그래야 사용자의 요청이 router 를 통해 들어와 ingress service 의 ip 또는 node ip :port 로 목적지를 식별 할 수 있을테니 말이다.    

문제는 picup 은 불가능하다. vagrant network 를 사용해서인지 router 에서 곧바로 Ingress proxy ip 를 찾지 못한다.    
그래서 꼼수를 쓰기로 했다. kubernetes port forwarding 을 사용하여 ingress service 를 다른 ip 가 아닌 실제 물리 pc 의 내부 ip, 즉 라우터가 물리 pc 에 할당한 ip 에 포트 포워딩하는 것으로 라우터가 목적지를 확인할 수 있도록 하는 것이다.    

요청 시나리오는 다음과 같다. 
1. 사용자가 요청한다.
2. 라우터는 사용자의 포트와 매핑된 내부 ip 와 포트를 찾아 요청을 넘긴다.
3. 물리 노드에 넘어온 요청이자 내부 ip 를 목적지를 갖는 요청은 kube port forwarding 에 의해 포트에 매핑된 ingress service ip, port 를 찾게 된다.
4. Ingress 로 넘어온 요청은 요청의 지표 (호스트 주소, path, 헤더 등)으로 다음 service 에 넘어간다.
5. Service 는 label 를 바탕으로 자신에게 연결된 pod 정보를 찾아 요청을 넘기게 된다.

```
kubectl port-forward -n kong service/kong-gateway-proxy 52080:80 --address 0.0.0.0
```

재밌는게 맨 뒤 address 옵션으로 port forwarding 으로 접속이 가능한 ip 범위를 지정할 수 있다. 이걸 몰라서 default (127.0.0.1) 로 두고 엄청 삽질했다.    
예시는 테스트를 위해 당장은 0.0.0.0 으로 하였는데 어느정도 네트워크 세팅이 마쳐지는대로 접속 가능 ip 범위를 라우터에 물린 내부 ip 정도로 좁힐 생각이다. 

```
while true:
do
    nohup kubectl port-forward -n kong service/kong-gateway-proxy 52080:80 --address 0.0.0.0 &
    sleep 5
done
```
kubectl port-forward는 한 프로세스이다. 오류로 종료되면 명령어가 다시 실행될 수 있게 반복한다. 
