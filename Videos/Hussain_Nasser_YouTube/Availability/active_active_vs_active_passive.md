Video: https://www.youtube.com/watch?v=d-Bfi5qywFo&list=PLQnljOFTspQVMeBmWI2AhxULWEeo7AaMC&index=16

# Active-Active Vs Active-Passive
Network configurations between servers to achieve better load balancing and high availaibility

## Active-Passive
1. We have 4 nodes (N) instances of a service running (may be on same host/container). 
2. We have two reverse proxying load balancers that forward the traffic to two sets of back-end servers. 
3. Everything is stateless
4. Both the proxies have different IP addresses but share the same virtual IP address.
5. One proxy is a back-up and one is master which assumes the virtual IP address
6. DNS A record points to virtual IP address.
7. One Active node (master) servicing the requests. and One is a Passive node.

### Pros:
1. Easier to setup
2. Easy and cheap

### Cons
1. Single server redirecting the requests
2. If a  single server goes down, slight delay is encountered to ARP the MAC address of the second server.

## Active-Active
1. Here it's a master set-up and two virtual IP address associated with the two masters
2. DNS A record points to two virtual IP addresses
3. The DNS query on the domain can give any of the IPs and gets redirected to any master node responsible for that IP.
4. This is highly scalable where each server answers requests.
5. In case one server goes down, the other server takes both virtual IPs.


### Pros
1. Well-balanced
2. No wastage of resources
3. Lower chances of one of the servers dying.

### Cons
1. This is not cheap. DNS does the round-robin.. 
2. Number of TCP connections that would be pre-warmed on a proxy would be higher than in the previous case.