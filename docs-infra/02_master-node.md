## Master node

#### Declare `NODE_IP`, `POD_NETWORK_CIDR` according to your configuration.

```
NODE_IP=${NODE_IP_ADDRESS}
POD_NETWORK_CIDR=172.16.0.0/16
```

#### kubeadm init 

```
sudo kubeadm init \
--pod-network-cidr=$POD_NETWORK_CIDR \
--apiserver-advertise-address=$NODE_IP \
--control-plane-endpoint=$NODE_IP
```

#### Set user env value

root user
```
export KUBECONFIG=/etc/kubernetes/admin.conf
```

regular user 
```
mkdir -p $HOME/.kube &&
sudo cp -i /etc/kubernetes/admin.conf $HOME/.kube/config &&
sudo chown $(id -u):$(id -g) $HOME/.kube/config
```

#### Check list
```
# Check pod network cidr
kubectl cluster-info dump | grep -m 1 cluster-cidr

# Check master node ip
kubectl get nodes -o wide

# Get join command
kubeadm token create --print-join-command
```

#### Get calico manifest file
```
CALICO_VERSION=3.25 &&
curl https://docs.projectcalico.org/archive/v$CALICO_VERSION/manifests/calico.yaml -O
```

#### Enable CALICO_IPV4POOL_CIDR and change the value as your pod network cidr
```
- name: CALICO_IPV4POOL_CIDR
  value: "172.16.0.0/16"
```

#### Apply
```
kubectl apply -f calico.yaml
```
