Video: https://www.youtube.com/watch?v=Q0irm6xzNNk (20:15)

# Cloudfare's Unimog (Only includes chief features and concept)

1. Reinvented what layer 4 load balancing is. 

## Load balancer
1. Subset of a reverse proxy that accepts connections and forwards requests to back-end servers by inspecting the content optionally.
2. If it's a TLS Terminating proxy, we can inspect the content at the level 7 and make routing decisions based on that.
3. Layer 4 can only look at the (source_ip, source_port, destination_ip, destination_port). Useful for stateful load, sticky load. 

## What is Unimog is.
1. Anycast addressing: Allows same IP address to be used across multiple data centres. So you have data centres in London, Amsterdam, Paris, they can have a server with the same IP address and depending on your location you get routed to the appropriate server (nearest's subset) 
2. DDoS is dead: Coordinated DoS attack from different sources can be prevented because each request won't be re-routed to the same location.
3. Virtual IP addresses: Assigning a same IP address to a cluster of servers thats associated with the cluster, but every machine has it's own direct IP address.
4. Forwarding Packets: The router actually forwards packets. ECMP technique allows the router to pick up different paths each time. Here the router does the load-balancing and we don't need an explicit load-balancer. ECMP ensure sends a correct packet to the correct server. To avoid issues, they added static load balancer software because of certain issues observed with the  router doing the load balancing.
5. Server as a load balancer: Unimog is installed on every server as a part of the Kernel that has load-balancing capabilities. The router can send every packet to every server and the server forwards the packet to the correct server. The servers use a control plane that tells each server the status of other servers 


## Walk through an actual request path
1. Request comes from Internet and goes to the router
2. Router uses ECMP and redirects it to a particular server. It's received by the kernel layer. 
3. This server says that this packet is for Virtual IP address and it's just one of us. So it takes care of redirecting this request to the actual direct IP of the appropriate server (by choosing an appropriate load balancing algorithm).
4. This server encapsulates the TCP packet in another packet and forwards it to the appropriate server.
5. This server processes the packet, sends it back to the source IP (virtual IP) to send it across the Internet.

## XDP module
1. XDP module at the kernel intercepts a packet received at the network interface passes it through a module that checks for DoS and drops right there if it is.
2. After this, the packet passes to a Unimod module which forwards it to the systems's TCP stack. 
3. If this packet is not for this machine, it queries the XDP control plane to find the destination IP to route the packet to.
4. XDP control plane contains the routing information


## Edge Computing
1. Front-end to back-end servers containing load balancers and proxies.

