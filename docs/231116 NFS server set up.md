## 쿠버네티스 볼륨과 NFS

쿠버네티스는 다양한 volume 형태를 지원한다.
empty dir 는 pod 의 생명 주기와 동일한 임시 공간을, Host path 는 pod가 생성된 worker 노드에 공간을 만든다.
이 밖에 AWS, GCP 와 같은 회사의 block storage나 NFS, Ceph, gitRepo 등 여러 타입을 선택 할 수 있다.

Picup에선 그 중 NFS를 사용하였다.
배포 환경에서 네트워크 비용을 아끼고자 스토리 서비스를 피하고 직접 내부 pc를 NFS 로 운영, k8s 에 마운트 한다.

거기에 NFS는 dynamic provisioning 을 지원하여 PV를 관리자가 직접 만들어두고 PVC로 호출하는 꼴을 피해,
NFS dynamic provisioning 처리 후에는 storage class 를 참조하는 것으로 동적으로 볼륨을 생성하고 이를 claim 할 수 있게 된다.
이 storage class 사용을 위한 동적 할당도 provisioner 가 필요한다.

```
sudo cat /proc/fs/nfsd/versions

# -2 +3 +4 +4.1 +4.2
```


## Server side

Sample shared directory path : `/shared`

### Make shared path
```
sudo mkdir /shared
sudo chmod 755 /shared
sudo chown -R nobody:nogroup /shared
```

(Permission denided 가 나오면다면 공유 폴더의 권한 모드 확인할 것)

### Install nfs-server

````
sudo apt-get update && sudo apt-get install nfs-kernel-server portmap
sudo systemctl restart nfs-server
service nfs-server status
````

### Configure access list
`sudo vi /etc/exports`

```
/shared *(rw,sync,insecure,no_root_squash,no_subtree_check,fsid=0)
```
- rw : 읽기, 쓰기
- sync : NFS가 응답 전 변경 내용을 기록함
- insecure : 인증되지 않는 엑세스 허용
- no_root_squash : client의 root 엑세스를 root 사용자로 인정
- no_subtree_check : 하위 트리 검사 비활성화
- no_root_squash : root 권한을 가진 작업이라도 권한이 없는 사용자로 변환한다.
- all_squash : 모든 사용자 권한을 익명 사용자 권한, nobody:nogroup으로 지정  -> This leads "Operation not permitted"

### Apply access list

```
sudo exportfs -a
sudo systemctl restart nfs-kernel-server
```

### Open Firewall

Check firewall status

```
sudo ufw status
```

Enable it if it's inactive

```
sudo ufw enable
```

Allow NFS acess port (default : 2049)
```
sudo ufw allow 2049
sudo ufw allow ${YOUR_SSH_PORT}
systemctl status nfs-server.service
```

## Client side

```
sudo apt-get install nfs-common
```

Sample mount directory path : `nfs1/mnt/shared1`

```
mkdir -p nfs1/mnt/shared1
sudo chmod 755 nfs1/mnt/shared1
```

Mount NFS shared directory on client mount path
```
sudo mount -t nfs 192.168.52.13:/shared nfs1/mnt/nfs_shared1
```

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