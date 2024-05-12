## Install kubernetes
- cpu : 2
- mem : 4GB
- OS : Ubuntu v22.04
- CRI : CRI-O  v1.28
- CNI : CALICO v3.25
- Kubernetes : v1.28

### Disable Swap
```
sudo -i &&
sudo swapoff -a &&
sudo sed -i '/ swap / s/^/#/' /etc/fstab
```

### Install CRI-O v1.28
```
export OS=xUbuntu_22.04 &&
export CRIO_VERSION=1.28 &&

echo "deb https://download.opensuse.org/repositories/devel:/kubic:/libcontainers:/stable/$OS/ /" | sudo tee /etc/apt/sources.list.d/devel:kubic:libcontainers:stable.list &&
echo "deb http://download.opensuse.org/repositories/devel:/kubic:/libcontainers:/stable:/cri-o:/$CRIO_VERSION/$OS/ /" | sudo tee /etc/apt/sources.list.d/devel:kubic:libcontainers:stable:cri-o:$CRIO_VERSION.list &&

curl -L https://download.opensuse.org/repositories/devel:kubic:libcontainers:stable:cri-o:$CRIO_VERSION/$OS/Release.key | sudo apt-key add - &&
curl -L https://download.opensuse.org/repositories/devel:/kubic:/libcontainers:/stable/$OS/Release.key | sudo apt-key add - &&

sudo apt update &&

sudo apt info cri-o &&
sudo apt install cri-o cri-o-runc &&

sudo systemctl start crio &&
sudo systemctl enable crio &&

sudo systemctl status crio
```

### Install Kubeadm, Kubelet, Kubectl v1.28

```
sudo apt-get update &&
sudo apt-get install -y apt-transport-https ca-certificates curl gpg &&

# If the directory `/etc/apt/keyrings` does exist, skip mkdir
sudo mkdir -p -m 755 /etc/apt/keyrings &&
curl -fsSL https://pkgs.k8s.io/core:/stable:/v1.28/deb/Release.key | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-apt-keyring.gpg &&

echo 'deb [signed-by=/etc/apt/keyrings/kubernetes-apt-keyring.gpg] https://pkgs.k8s.io/core:/stable:/v1.28/deb/ /' | sudo tee /etc/apt/sources.list.d/kubernetes.list &&

sudo apt-get update &&
sudo apt-get install -y kubelet kubeadm kubectl &&
sudo apt-mark hold kubelet kubeadm kubectl
```

### Enable br_filter & ip_forward
```
modprobe br_netfilter &&
echo '1' > /proc/sys/net/ipv4/ip_forward
```

### Initialize kubeadm
```
NODE_IP=${NODE_IP_ADDRESS}
POD_NETWORK_CIDR=${POD_CIDR}
#ex, NODE_IP=192.168.0.101
#ex, POD_NETWORK_CIDR=172.16.0.0/16

sudo kubeadm init \
--pod-network-cidr=$POD_NETWORK_CIDR \
--apiserver-advertise-address=$NODE_IP \
--control-plane-endpoint=$NODE_IP
```

### Install Calico

#### Get calico.yaml
```
CALICO_VERSION=3.25 &&
curl https://docs.projectcalico.org/archive/v$CALICO_VERSION/manifests/calico.yaml -O
```

#### Enable CALICO_IPV4POOL_CIDR and change the value as your pod network cidr
```
- name: CALICO_IPV4POOL_CIDR
  value: "${POD_NETWORK_CIDR}"   #ex, value : "172.16.0.0/16"
```

#### Apply calico
```
kubectl apply -f calico.yaml
```

### Make schedule pods on the master
```
kubectl taint nodes --all node-role.kubernetes.io/control-plane-
```

### Install k9s
```
K9S_VERSION=v0.26.7
curl -sL https://github.com/derailed/k9s/releases/download/${K9S_VERSION}/k9s_Linux_x86_64.tar.gz | sudo tar xfz - -C /usr/local/bin k9s
```

### Install helm
```
curl -fsSL -o get_helm.sh https://raw.githubusercontent.com/helm/helm/main/scripts/get-helm-3
chmod 700 get_helm.sh
./get_helm.sh
```

### NFS provisioner
```
sudo apt install nfs-common

helm repo add nfs-subdir-external-provisioner https://kubernetes-sigs.github.io/nfs-subdir-external-provisioner
helm install nfs-subdir-external-provisioner nfs-subdir-external-provisioner/nfs-subdir-external-provisioner \
    --set nfs.server=x.x.x.x \
    --set nfs.path=$PATH
```

## Join worker node

### Previous procedure
- Disable swap
- Install CRI-O
- Install Kubeadm, Kubelet, Kubectl
- Enable br_filter & ip_forward

### Get join command [Master]
```
kubeadm token create --print-join-command
```
