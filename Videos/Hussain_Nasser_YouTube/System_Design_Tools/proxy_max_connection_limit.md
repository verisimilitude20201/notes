Video: https://www.youtube.com/watch?v=Cuz5WOSXE_s&list=PLQnljOFTspQVMeBmWI2AhxULWEeo7AaMC&index=27(10:00)

# Is there a limit to the number of connections to the back-end or proxy?

1. Consider a client talking to a proxy which is load-balancing between a fleet of servers.
2. Between the client and load balancer there is virtually no limit given you have enough memory, RAM and CPU. 3. Destination IP is address is fixed and destination port is fixed. So the only thing that changes is the source port since source IP (reverse proxy) IP is also fixed (assuming a static IP). There are 65536 alternatives for choosing a source port.
3. Between the load balancer and backends there's technically one client with same source IP and source port making connections to a back-end server. At the max we can have 65536 connections to a specific back-end. More back-ends you add it doubles up.
4. We do connection reusability. We can have 100s of TCP connections which can theoratically service thousands of clients. We can define here a max limit of 100 connections.
5. We can also have multiple network interfaces with different IP addresses so as to have more connections.
6. Should'nt worry about this if you have a statless load. For GRPC, RTMP, MySQL connection, Websockets the client tethers itself to a single connection. If you use Websockets, the number will go high quickly.
7. More TCP connections more work for the kernel. Proxies use tricks to manage this more efficiently. Envoy for example uses HTTP/2 at the back-end even HTTP/1 and uses HTTP stream to send your request per back-end. The limit on number of HTTP streams per connection is 100. We have one connection, multiplexing. HTTP/2 adds some overhead the more number of streams you have since it's user space.
8. CONNECT opens a tunnel between the client and back-end server via a proxy. CONNECT is limited, it's dumb it just sends a stream of data to the back-end. For every CONNECT, a TCP connection is reserved just for you. It's a stateful thing.
9. RFC 8441 solves this problem by introducing a extended CONNECT. So we tunnel on a stream on a connection depending on a protocol. For a database connection, there is no multiplexing. Proxies are refusing to implement this.
10. MASQUE (Multiplexed Application Substrate Over Quic encryption) is another protocol that addresses drawbacks of CONNECT which works only for TCP and MASQUE handles UDP as well.
11. What protocol is efficient is depends on the client. HTTP/2 is useful when a client wants to request multiple things at the same time. If that is not your use-case, HTTP/2 may not be the protocol for you.
