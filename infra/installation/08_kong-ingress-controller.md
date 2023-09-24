### Install with helm
```
helm repo add kong https://charts.konghq.com
helm repo update

# Helm v3+
helm install kong/kong --generate-name --set ingressController.installCRDs=false -n kong --create-namespace
```

### Request to Kong Gateway

#### Find kong proxy ip and port

```
HOST=$(kubectl get svc --namespace kong {KONG_PROXY_SVC_NAME} -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
PORT=$(kubectl get svc --namespace kong {KONG_PROXY_SVC_NAME} -o jsonpath='{.spec.ports[0].port}')
export KONG_PROXY=${HOST}:${PORT}
curl $KONG_PROXY
```

{"message":"no Route matched with those values"}

     
### To proxy requests,

#### Create routing configuration to proxy /echo requests to the echo server

```
kubectl apply -f https://bit.ly/echo-service

echo "
apiVersion: networking.k8s.io/v1
kind: IngressClass
metadata:
  name: kong
spec:
  controller: ingress-controllers.konghq.com/kong
" | kubectl apply -f -

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
              number: 80
" | kubectl apply -f -
```

#### Test the routing rule

```
KONG_PROXY_IP=$(kubectl get svc --namespace kong kong-1686487324-kong-proxy -o jsonpath='{.status.loadBalancer.ingress[0].ip}')
curl -i http://kong.example/echo --resolve kong.example:80:${KONG_PROXY_IP}
```

### Docs
installation : `https://docs.konghq.com/kubernetes-ingress-controller/2.8.x/deployment/overview/`    
how to use   : `https://docs.konghq.com/kubernetes-ingress-controller/2.8.x/guides/getting-started/`

### Strip path

strip_path can be configured to strip the matching part of your path from the HTTP request before it is proxied.   

If it is set to "true", the part of the path specified in the Ingress rule will be stripped out before the request is sent to the service. For example, when it is set to "true", the Ingress rule has a path of /foo and the HTTP request that matches the Ingress rule has the path /foo/bar/something, then the request sent to the Kubernetes service will have the path /bar/something.
