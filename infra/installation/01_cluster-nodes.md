## Commons 

#### Disable swap
```
sudo -i
swapoff -a
echo 0 > /proc/sys/vm/swappiness
sed -e '/swap/ s/^#*/#/' -i /etc/fstab
```

#### Install crio v1.23
```
sudo apt update -y && 
sudo apt upgrade -y &&

OS=xUbuntu_20.04 &&
CRIO_VERSION=1.23 &&

echo "deb https://download.opensuse.org/repositories/devel:/kubic:/libcontainers:/stable/$OS/ /"|sudo tee /etc/apt/sources.list.d/devel:kubic:libcontainers:stable.list &&
echo "deb http://download.opensuse.org/repositories/devel:/kubic:/libcontainers:/stable:/cri-o:/$CRIO_VERSION/$OS/ /"|sudo tee /etc/apt/sources.list.d/devel:kubic:libcontainers:stable:cri-o:$CRIO_VERSION.list &&

curl -L https://download.opensuse.org/repositories/devel:kubic:libcontainers:stable:cri-o:$CRIO_VERSION/$OS/Release.key | sudo apt-key add - &&
curl -L https://download.opensuse.org/repositories/devel:/kubic:/libcontainers:/stable/$OS/Release.key | sudo apt-key add - && 

sudo apt update -y &&
sudo apt install cri-o cri-o-runc -y &&

sudo systemctl enable crio.service &&
sudo systemctl start crio.service &&

systemctl status crio
```

#### Install Kubeadm, Kubelet, Kubectl
```
sudo apt-get update -y &&
sudo apt-get install -y apt-transport-https ca-certificates curl &&

mkdir -p /etc/apt/keyrings &&
curl -fsSL https://packages.cloud.google.com/apt/doc/apt-key.gpg | sudo gpg --dearmor -o /etc/apt/keyrings/kubernetes-archive-keyring.gpg &&
echo "deb [signed-by=/etc/apt/keyrings/kubernetes-archive-keyring.gpg] https://apt.kubernetes.io/ kubernetes-xenial main" | sudo tee /etc/apt/sources.list.d/kubernetes.list && 

sudo apt-get update &&
sudo apt-get install -y kubelet kubeadm kubectl &&
sudo apt-mark hold kubelet kubeadm kubectl
```

#### Network config
```
modprobe br_netfilter &&
echo '1' > /proc/sys/net/ipv4/ip_forward
```

#### Declare node-ip on kubelet environment values
```
NODE_IP=${NODE_IP_ADDRESS}
echo "KUBELET_EXTRA_ARGS=\"--node-ip=$NODE_IP\"" >> /etc/default/kubelet
```

Declare `NODE_IP` according to your VM settings. Without this task, the node ip registered to the cluster is set to the default NAT ip address(10.0.2.15).
