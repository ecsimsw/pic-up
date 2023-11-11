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



