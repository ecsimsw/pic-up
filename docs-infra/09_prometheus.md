### Install with helm
```
helm repo add prometheus-community https://prometheus-community.github.io/helm-charts
helm repo update

helm install prometheus prometheus-community/prometheus -f prometheus-helm-values.yaml -n prometheus --create-namespace
```

#### If you want to test without persistent volume
```
server:
  persistentVolume:
    enabled: false ## If false, use emptyDir
```

#### If you have host only for promethues server
```
server:
  ingress: 
    enabled: true
    ingressClassName: kong
    hosts: [ prometheus.ecsimsw.com ] 
    path: /
```

#### Custom Ingress

Prometheus might be expecting to have control over the root path (/). (ex, prometheus.ecsimsw.com/)        
If you want to balance request with sub path, create customized ingress like bellow (ex, kube.ecsimsw.com/prometheus). 

```
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: prometheus-server-ingress
  namespace: prometheus
  annotations:
    konghq.com/strip-path: 'true'
spec:
  ingressClassName: kong
  rules:
    - host: kube.ecsimsw.com
      http:
        paths:
          - path: /prometheus
            pathType: ImplementationSpecific
            backend:
              service:
                name: prometheus-server
                port:
                  number: 80
```

### Check web ui
1. Enter web ui on POD[name: prometheus-server, port: 9090], SERVICE[name: prometheus-server, port: 80] or INGRESS you made. (In my case, kube.ecsimsw.com/prometheus)
2. Go `status` -> `targets` -> `kubernetes-nodes` 
3. Check all the nodes' status are `up`
