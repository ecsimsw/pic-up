#### Check if you are using IPVS mode
If you’re using kube-proxy in IPVS mode, since Kubernetes v1.14.2 you have to enable strict ARP mode.

```
kubectl edit configmap -n kube-system kube-proxy           
```

and set ipvs.strictARP as true

```
apiVersion: kubeproxy.config.k8s.io/v1alpha1
kind: KubeProxyConfiguration
mode: "ipvs"
ipvs:
  strictARP: true
```

#### Installation With Helm
```
helm repo add metallb https://metallb.github.io/metallb

helm install metallb metallb/metallb -n metallb-system --create-namespace
or
helm install metallb metallb/metallb -f metallb-v0.13.7-helm-values.yaml -n metallb-system --create-namespace
```

`https://metallb.universe.tf/installation/`

### Sample 

##### IPAddressPool
```
apiVersion: metallb.io/v1beta1
kind: IPAddressPool
metadata:
  name: external-ip-pool-2
  namespace: metallb-system
spec:
  addresses:
    - 192.168.52.20-192.168.52.29
---
apiVersion: metallb.io/v1beta1
kind: IPAddressPool
metadata:
  name: external-ip-pool-3
  namespace: metallb-system
spec:
  addresses:
    - 192.168.52.30-192.168.52.39
```

#### L2Advertisement
```
apiVersion: metallb.io/v1beta1
kind: L2Advertisement
metadata:
  name: l2-advertisement
  namespace: metallb-system
spec:
  ipAddressPools:
  - external-ip-pool-2
  - external-ip-pool-3
```

#### Do I need service load balancer type, if I'm already using ingress?

```
For example:

Supose you have 10 services of LoadBalancer type: This will result in 10 new publics ips created and you need to use the correspondent ip for the service you want to reach.

But if you use a ingress, only 1 IP will be created and the ingress will be the responsible to handle the incoming connection for the correct service based on PATH/URL you defined in the ingress configuration. With ingress you can:

Use regex in path to define the service to redirect;
Use SSL/TLS
Inject custom headers;
Redirect requests for a default service if one of the service failed (default-backend);
Create whitelists based on IPs
Etc...
```

