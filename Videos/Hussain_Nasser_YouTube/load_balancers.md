Video: https://www.youtube.com/watch?v=aKMLgFVxZYk&list=PLQnljOFTspQWdgYcGXCTkjda8vd2jWJYt&index=1(9:14)

# Load balancing - Layer 4 Vs Layer 7
-----------------------------------

1. Load balancing is the balancing of incoming requests to multiple machines, services, processes. 
2. Load balancer is nothing but a reverse proxy.
3. Software that hosts on a machine. Load balancer decides on a lot of criteria where to forward (which server) to request to.

## Load balancer types: 
-------------------

## Layer 4 Load Balancer - 
---------------------

1. Layer 4 load balancer knows only the IP and port. It does not take decisions based on data. 
   a. Consider client (IP: 44.1.1.1), Load-balancer(IP: 44.2.2.2), Server 1 (IP: 44.3.3.3) & Server 2(44.4.4.4)
   
   b. Client(IP: 44.1.1.1) contacts the load-balancer (IP: 44.2.2.2). The IP packet that gets sent is 44.1.1.1|Data|44.2.2.2
   
   c. Load balancer depending on the set algorithm decides to forward this request to Server 1 (IP: 44.3.3.3). It changes the destination IP of the packet to 44.1.1.1|Data|44.3.3.3 and adds an entry to a lookup table (NAT) so that the response is returned via the same route.
   
   d. The connection between the client <-> load balancer <-> server is one single TCP connection.
   
   e. Server does not know the IP address of the client that connected to it. There can be headers in the request that may give ot the IP Address too

2. There can be headers associated with the proxy that tells the server about the client. But at layer 4, it's only IP address and port.

## Pros:
----

1. Simpler load balancing
2. Efficient because no data look-up. 
3. More secure because it does not need to look at the data. If Layer 4 data gets compromised, we have just the packets that are encrypted mostly.
4. One single TCP connection.
5. Uses NAT. Statefulness

## Cons:
----
1. No smart load balancing. Cannot make smart decisions based on data. Cannot modify, cannot rewrite URLs
2. Not applicable for microservices. Ingress protocol based on path redirects to different microservices
3. Sticky per segment. For a TCP connection, there is an maximum transmission unit (MTU) say 1500 bytes. For a GET request that's MB response, it needs to broken into multiple segments. One TCP packet split into multiple TCP segments. Load balancer cannot forward this segments to different servers.
4. No caching because cannot look at the data.

## Configuring an HAProxy
--------------------- 

    backend nodes
    server server1 127.0.0.1:4444 check
    server server2 127.0.0.1:5555 check

    frontend localnodes
    bind *:8888
    default_backend nodes

    global
    maxconn 940


Need to specify same timeout for client and server, it's just one TCP connection


## Layer 7 Load balancing
---------------------
1. It is authorized to see application data.
2. It establishes a connection between the load balancer and client and load balancer and server and can decrypt your stuff
3. Consider that we have a service dedicated for serving pictures. Client (IP: 44.1.1.1), Load balancer(IP: 44.2.2.2), Pictures service (44.3.3.3) and comments service (44.4.4.4)

   a. Client makes a TCP connection between itself and load balancer. It requests pictures. 44.1.1.1 | GET /pictures | 44.2.2.2
   
   b. There's a rule configured that says when /pictures, redirect to Pictures service (44.3.3.3)
   c. It creates a new TCP connection 44.2.2.2 | GET /pictures | 44.3.3.3. 
   
   d. There can be two separate TLS handshakes happening between the client and load balancer and load balancer and the backend.
   
   e. For comments, we can derive a similar reasoning.


## Pros
----

1. Smart load balancing. Microservice stuff, make data based decisions. Pretty powerful
2. Can do caching.
3. Great for microservices


## Cons
----
1. Expensive (looks at data). Most of times this is insignificant because our machines are very powerful. Using a Raspberry Pi as a Layer 4 load balancer Vs Layer 7 can make is still feel the difference.

2. Decrypts the data (terminates the TLS). Server Name indication may be used to serve different connections, serving different certificates to serve multiple domains.

3. Two TCP connections. Can be good or bad. Bad because more number of connections. Timeouts are critical. We can share different clients through the same connection. Can pool TCP connections.

4. Must share TLS certificate.


## HAProxy example:
---------------

    frontend localnodes

    bind *:9999
        mode http
        acl app1 path_end -i /app1
        acl app2 path_end -i /app2
        use_backend app1servers if app1
        use_backend app2servers if app2

    backend app1servers
        mode http
        server server1 127.0.0.1:4444 check
        server server2 127.0.0.1:5555 check


    backend app2servers
        mode http
        server server1 127.0.0.1:8888 check
        server server2 127.0.0.1:9999 check