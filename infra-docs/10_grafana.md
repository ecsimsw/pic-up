### Install grafana v9.4.2
```yaml
kubectl apply -f manifests/grafana-v9.4.2.yaml
```

or customize bellow.

```yaml
apiVersion: v1
kind: Namespace
metadata:
  name: grafana
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: grafana
  namespace: grafana
spec:
  replicas: 1
  selector:
    matchLabels:
      app: grafana
  template:
    metadata:
      name: grafana
      labels:
        app: grafana
    spec:
      containers:
        - name: grafana
          image: grafana/grafana:9.4.2
          ports:
            - name: grafana
              containerPort: 3000
          env:
            - name: GF_SERVER_HTTP_PORT
              value: "3000"
            - name: GF_AUTH_BASIC_ENABLED
              value: "false"
            - name: GF_AUTH_ANONYMOUS_ENABLED
              value: "true"
            - name: GF_AUTH_ANONYMOUS_ORG_ROLE
              value: Admin
            - name: GF_SERVER_ROOT_URL
              value: /
---
apiVersion: v1
kind: Service
metadata:
  name: grafana-service
  namespace: grafana
  annotations:
    prometheus.io/scrape: 'true'
    prometheus.io/port: '3000'
spec:
  ports:
    - port: 3000
      targetPort: 3000
  selector:
    app: grafana
---
apiVersion: networking.k8s.io/v1
kind: Ingress
metadata:
  name: grafana-ingress
  namespace: grafana
  annotations:
    konghq.com/strip-path: 'true'
spec:
  ingressClassName: kong
  rules:
    - host: grafana.ecsimsw.com
      http:
        paths:
          - path: /
            pathType: ImplementationSpecific
            backend:
              service:
                name: grafana-service
                port:
                  number: 3000
```

### Add data sources

1. Enter web ui on POD[name: grafana, port: 3000], SERVICE[name: grafana-service, port: 3000] or INGRESS you made. (in my case, grafana.ecsimsw.com)   
2. Login with default user [id: admin, pw: admin]   
3. Add data sources ( `Configuration` > `Data sources` > `Add data sources` )   
