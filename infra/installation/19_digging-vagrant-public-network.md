# Digging vagrant public network

## Vagrant public network settings
master : vm.network "public_network", bridge: "wlo1", ip: "192.168.0.100"
worker1 : vm.network "public_network", bridge: "wlo1", ip: "192.168.0.101"
worker2 : vm.network "public_network", bridge: "wlo1", ip: "192.168.0.102"

bridge : "wlo1" / one of interface on vagrant host server (inet 192.168.0.18/24)

## Connection test

1. The connection between nodes using "wlo1" interface is ok. (192.168.0.1/24)
```
vm-master(192.168.0.100) <-> vm-worker1(192.168.0.101)
mac-mini(192.168.0.20) <-> vm-master(192.168.0.100)
mac-book(192.168.0.22) <-> vagrant-host(192.168.0.18)
```

2. Issue on accessing with using router port forwarding.
Nodes directly connected to the router are able to be accessed from the internet with external ip, but virtual machines are not.

```
internet -> router pf -> vagrant-host process (192.168.0.18:80)  OK
internet -> router pf -> mac-mini process (192.168.0.20:80)      OK
internet -> router pf -> mac-book process (192.168.0.22:80)      OK

internet -> router pf -> vm-master process (192.168.0.100:80)    UNREACHABLE
internet -> router pf -> vm-worker1 process (192.168.0.101:80)   UNREACHABLE
```

## Next step
It seems there are two separate networks (router to real machines) and (real machines to virtual machines).

The easiest way is to use kubernetes port forwarding on ingress proxy service at real machine and router grep that port.
But I'm going to try to build a network of (routers - real nodes - virtual machines) for now.

## Port forward for now
```
kubectl port-forward service/${kong-proxy} -n kong 80:80 --address='0.0.0.0'
```
