Video: https://www.youtube.com/watch?v=npnqyRT77Zc

# VPN and Proxy

1. Both VPN and proxies make requests on behalf of a client.

## How does VPN work?

1. Typical network setup: 
    - Network A: Router with a public IP and private IP. That also is a DHCP that gives you public IP
    - Network B: Also an independent network having it's own setup of private and public IP addresses.
    - Router transfers a packet to google.com via it's public interface. Google knows the public IP address.
    - A host within network A cannot access host within network B.

2. VPN literally makes you a part of private network. VPN uses layer 2 tunneling protocol (L2TP), IPsec, Internet Key Exchange Protocol(IKEv2). 
3. Once a VPN tunnel is established, the host becomes a part of the network. If the protocol supports encryption, each packet exchanged between the two hosts in two different networks is encrypted. 
4. The router simply forwards the packet to the VPN tunnel.

### VPN Pros
1. Encryption by default. Encrypts at the lowest level.
2. Redirects all traffic at the lowest level layer 2.
3. Access restricted websites. People watch Netflix shows that are banned in a country by using a VPN say NordVPN.
4. Access private networks (WFH)

### VPN Cons
1. Not completely anonymous. VPN can log your data/IP.
2. One extra hop to the VPN and VPN makes request on your behalf. Somewhat slow.
3. Double encryption: 99.99 % websites are https. On top of that, VPN encryption. Slowness because of that.
4. VPN can log all sorts of data if they are unencrypted. They can see the domain, IP. But they cannot see what you search on google.com
5. No caching.
6. Moment you become a part of a private network, the other nodes in the network can use the resources you're sharing.

# How HTTP Proxy works.
1. Proxy makes a HTTP requests on behalf of a client. It looks at the Host header to understand where to redirect the user to. 
2. Proxy just like VPN can see all sorts of data if it's unencrypted.
3. Proxies are protocol-specific. ICMP ping requests don't use proxies

## HTTP Proxy pros
1. Caching (Pure HTTP). In this case, however proxies can see what you are requesting.
2. Works at layer 4 and layer 7 perspective.
3. Can be anonymous. Public HTTP proxies can use various forwarded-for headers that can trace you to your destination.
4. Blocking websites (ISP transparent layer 4 proxies.)
5. Many application (load balances, http accelerators, service mesh, load balancing, firewall proxy security)

## PRoxy cons
1. Application can bypass the proxy.Certain proxies (Fiddler) can intercept requests and see what your requesing.
2. Not all proxy is forwarded. HTTP Proxy transfers only HTTP traffic.
3. No encryption by default.
4. More dangerous than VPN.A public HTTPS proxy can see your data. They are TLS terminating, they serve you their certificate instead of the destination website certificate. Kazakhstan government intercepts all traffic by using HTTPS proxy. The government can see everything.