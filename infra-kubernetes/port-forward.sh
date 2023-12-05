# kong ingress proxy
kubectl port-forward -n kong service/kong-gateway-proxy 52080:80 --address 0.0.0.0

# picup services only for inner networks / NEED TO BE CHANGED ALLOW ADDRESS
kubectl port-forward -n picup service/member-server-svc 528082:8082 --address 0.0.0.0
kubectl port-forward -n picup service/storage-server-svc 528083:8083 --address 0.0.0.0
kubectl port-forward -n picup service/album-server-svc 528084:8084 --address 0.0.0.0
