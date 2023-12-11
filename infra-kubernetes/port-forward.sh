# kong ingress proxy
nohup kubectl port-forward -n kong service/kong-gateway-proxy 52080:80 --address 0.0.0.0 &

# picup services only for inner networks / NEED TO BE CHANGED ALLOW ADDRESS
# From now, this is allow all the address. This should not be localhost cause docker internal address
nohup kubectl port-forward -n picup service/member-server-svc 52082:8082 --address 0.0.0.0 & \
nohup kubectl port-forward -n picup service/storage-server-svc 52083:8083 --address 0.0.0.0 & \
nohup kubectl port-forward -n picup service/album-server-svc 52084:8084 --address 0.0.0.0 & \
nohup kubectl port-forward -n monitoring service/kube-state-metrics 52090:8090 --address 0.0.0.0 & \
nohup kubectl port-forward -n monitoring service/node-exporter-http 52091:9100 --address 0.0.0.0 &

# ps -ef | grep "kubectl port"