Video: https://www.youtube.com/watch?v=Zgy1miPsTNs&list=PLQnljOFTspQVMeBmWI2AhxULWEeo7AaMC&index=11

# Failover and Availaibility
1. Failover is a technique of switching over to a different back-end machine when a certain machine goes down.
2. Common High availaibility technique and used with a mix of layer-4 and layer-7 load balancing.


## What is fail-over?
1. Client connects to a machine which is a server that connects to a database and returns results. 
2. When that machine goes down, it's automatically redirected to a different server. This entire transition is transparent to the client.


## Address Resolution Protocol
1. As software engineers, IP addresses is the lowest level that we use.
2. Machines deal with MAC address (Media address control).
3. ARP converts MAC addresses and IP addresses. It asks this question "Who has the IP address for 10.0.0.1"?
4. The actual machine will answer this saying it's me!

## Virtual IP address
1. Let's say we have two IP addresses 10.0.0.1 (Mac AAA) and 10.0.0.2 (Mac BBB) on two machines. They can heartbeat to each other.
2. They have software that can communicate with each other and have agreements on several things. They agree on a master node and the back-up nodes.
3. They also agree on a single, virtual IP address that does'nt exist in the real world.One of the nodes takes the responsibility of responding to all requests for the IP address and other node acts as the back-up. 
4. The software does this entire trick of virtual IP addresses.