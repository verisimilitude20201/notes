Video:

# Cloudfare's Unimog

1. Reinvented what layer 4 load balancing is. 

## Load balancer
1. Subset of a reverse proxy that accepts connections and forwards requests to back-end servers by inspecting the content optionally.
2. If it's a TLS Terminating proxy, we can inspect the content at the level 7 and make routing decisions based on that.
3. Layer 4 can only look at the (source_ip, source_port, destination_ip, destination_port). Useful for stateful load, sticky load. 

## What is Unimog is.
1. Anycast addressing: Allows same IP address to be used across multiple data centres. So you have data centres in London, Amsterdam, Paris, they can have a server with the same IP address and depending on your location you get routed to the appropriate server (nearest's subset) 
2. DDoS is dead: Coordinated DoS attack from different sources can be prevented because each request won't be re-routed to the same location.
3. Virtual IP addresses: Assigning a same IP address to a cluster of servers, but every machine has it's own direct IP address.
4. Forwarding Packets: 