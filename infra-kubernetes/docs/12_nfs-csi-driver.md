## NFS subdir external provisioner

`https://github.com/kubernetes-sigs/nfs-subdir-external-provisioner`

```
helm repo add nfs-subdir-external-provisioner https://kubernetes-sigs.github.io/nfs-subdir-external-provisioner/
helm install nfs-subdir-external-provisioner nfs-subdir-external-provisioner/nfs-subdir-external-provisioner \
    -n nfs-provisioner --create-namespace\
    --set nfs.server=${SERVER_IP}\
    --set nfs.path=${SERVER_SIDE_MOUNT_PATH}

# ex
helm install nfs-subdir-external-provisioner nfs-subdir-external-provisioner/nfs-subdir-external-provisioner \
    -n nfs-provisioner --create-namespace\
    --set nfs.server=192.168.52.13\
    --set nfs.path=/shared
```

### ERROR : MountVolume.SetUp failed │  exit status 32

#### error
```
MountVolume.SetUp failed for volume "nfs-subdir-external-provisioner-root" : mount failed:  │  exit status 32
```

#### sol
install nfs-common in every Kubernetes nodes.
```
sudo apt-get install -y nfs-common
```
