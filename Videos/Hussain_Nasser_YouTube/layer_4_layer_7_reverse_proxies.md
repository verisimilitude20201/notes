Video: https://www.youtube.com/watch?v=ylkAc9wmKhc&list=PLQnljOFTspQVMeBmWI2AhxULWEeo7AaMC&index=6(12:58)

# Layer 4 Vs Layer 7 Reverse proxies.
Imagine a single client, single reverse proxy and two back-ends S1 and S2.

## Layer 7 reverse proxies.
1. Client sends a GET / to the reverse proxy. This translates to a bunch of packets
2. Reverse proxy receives all packets and acknowledges them. When it receives all, it acknowledges the request. Now, the proxy understands that the request is for GET /
3. Let's say the load-balancing algorithm is round-robin. The proxy selects a server S1 and initiates a new TCP connection to it. Certain proxies may have already created a pool of TCP connections here. 
4. Proxy again splits the request into 3 new packets and sends it to S1. 
5. If S1 received all the 3 packets then only it acks it and starts making sense and processing the request
6. Response assembling and forwarding to the client takes place via the same two connections to the client.
7. All this while the client will wait for the results.
8. Since HTTP is a stateless protocol, it can jump from S1 to S2 and S2 to S1 respectively, there is no state.