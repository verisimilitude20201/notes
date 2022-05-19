# Maglev

## Abstract

1. Network load balancer running on commodity linux servers
2. No special hardware or specialized physical rack deployment
3. Able to saturate a 10 GBPS link with small packets
4. Equipped with consistent hashing & connection tracking features to minimize the impact of unexpected failures and faults on connection oriented protocols. 
5. Serving Google's traffic since 2008. 


## Introduction
1. A network load balancer is typically composed of multiple devices logically located between routers and service endpoints (generally TCP/UDP servers)
2. Traditionally implemented as hardware network load balancers with a few drawbacks
    - Scalability is constrained by the maximum capacity of a single unit
    - Often do not meet Google's requirements for high availaibility even though they provide 1 + 1 redundancy.
    - Lack flexibility and programmability since it's difficult to modify a hardware load balancer.
    - Costly to upgrade.
3. Therefore, Google implemented the network load balancer as a distributed software system. It offered the following advantages
    - Can adopt the scale-out model. To scale, the number of instances of load balancer can be increased.
    - Availaibility and reliability are enhanced, since this provides N + 1 redundancy.
    - Quick to add, test & deploy
    - We can also divide services between shards of load balancers to achieve performance isolation.

4. Designing a software network load balancer is complex. Few requirements
    - Throughput of each node in the system should be high. The capacity of the system is N * T where N is the number of nodes and T is the throughput of each node.
    - Connection persistence should redirect packets belonging to one connection to a specific end-point.


## System overview

### Front-end serving architecture of Google
1. Every Google service has a Virtual IP Address (VIP) which is served by a set of multiple service endpoints behind Maglev. 
2. This VIP which is associated by Maglev with the set of services is announced to the Google routers over BGP which is in turn announced to the Google's back-bone.
3. How a request goes through
    - User tries to access a Google service at http://www.google.com. Her browser issues a DNS query which is responded to by one of Google's authoritative DNS servers
    - DNS servers assigns the user to a nearby front-end location depending on the user's geolocation & current load at each location and returns a VIP
    - The browser tries to establish a connection with the VIP. 
    - The router tries to forward the packet to one of the Maglev machines through ECMP.
    - Maglev selects a service endpoint from the list of configured endpoints and forwards the packet after encapsulating it with Generic Routing Encapsulation (GRE)
    - The service endpoint decapsulates the packet, processes it and forwards it directly to the router from where the response is forwarded to the user. Maglev does'nt have to deal with outgoing packets. This is Direct Server Return (DSR)


### Configuration
1. Each Maglev unit is responsible for announcing VIPs to the routers (BGP) and forwarding the connection traffic to one of the service endpoints. 
2. It has two components: Controller and Forwarder.
3. On each Maglev machine, the controller periodically checks the health status of the forwarder to ensure that the router forwards packets to only healthy Maglev machines.
4. At the forwarder, each VIP is configured with one or more back-end pools. These are service endpoints unless specified otherwise.
5. Each back-end pool depending on it's requirements has health service endpoints to ensure packets are forwarded to only the healthy endpoints. Same server may be included in multiple back-end pools so the health checks are de-dup by the IP address
6. It is possible to deploy multiple shards of Maglevs in the same cluster. DifferentMaglev shards are configured differently and serve different sets of VIPs. Sharding is
useful for providing performance isolation and ensuring quality of service.


## The Forwarder - The design & Implementation
