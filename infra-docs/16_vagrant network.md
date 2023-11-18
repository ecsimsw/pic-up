# Three types of vagrant network

ref, https://blog.jeffli.me/blog/2017/04/22/vagrant-networking-explained/

### Home Networking Topology
We assume that Vagrant is running in the laptop whose IP address is `192.168.1.10`.

### Public Networking (bridged networking)
The word `public` does not mean that the IP addresses have to be [public routable IP addresses](https://en.wikipedia.org/wiki/IP_address#Public_address). Hence it is kind of confusing. The [doc](https://www.vagrantup.com/docs/networking/public_network.html) also states that `bridged networking`would be more accurate. In fact, `public networking` is called `bridged networking` in the early days—see the [doc of old version](http://docs-v1.vagrantup.com/v1/docs/bridged_networking.html). Just keep one simple rule in mind—if the VM’s assigned IP is in the same subnet of the host, then it is a public networking configuration.

![image](https://github.com/ecsimsw/ecsimsw-kube/assets/46060746/bad38c41-d01e-407d-8651-6dee2fe7cd06)

### Private Networking (virtual router)

![image](https://github.com/ecsimsw/ecsimsw-kube/assets/46060746/7710fa39-5311-42c3-b60a-17764c4ef7c1)

### Forwarded Ports 

port forwarding without router

![image](https://github.com/ecsimsw/ecsimsw-kube/assets/46060746/7f4ec1ca-135e-400c-b3e8-fc8e3b7adc83)
