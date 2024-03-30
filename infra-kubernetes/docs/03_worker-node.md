## Worker node

#### Get join command from master node
```
kubeadm token create --print-join-command
```

#### Kubeadm join
```
ex, kubeadm join 192.168.52.10:6443 --token 6iilcp.2uh68yajgxq95atw --discovery-token-ca-cert-hash sha256:4d2e33fa31dfd4596d76bc570f8849052b163e62662021ad6f344bdb2a3544cf 
```

#### Run 'kubectl get nodes' on master 

```
kubectl get nodes -o wide
```
