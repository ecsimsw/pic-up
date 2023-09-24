### Test with dns resolution
[k8s/dns-debugging-resolution](https://kubernetes.io/docs/tasks/administer-cluster/dns-debugging-resolution/)

#### Create dns utils pod
```
echo "apiVersion: v1
kind: Pod
metadata:
  name: dnsutils
  namespace: default
spec:
  containers:
  - name: dnsutils
    image: registry.k8s.io/e2e-test-images/jessie-dnsutils:1.3
    command:
      - sleep
      - "infinity"
    imagePullPolicy: IfNotPresent
  restartPolicy: Always" | kubectl apply -f -
```

#### check `nslookup` works

```
kubectl exec -it dnsutils /bin/bash 
nslookup google.com
```
