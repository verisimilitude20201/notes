Video: https://www.youtube.com/watch?v=uNjACLXoH5A&list=PLQnljOFTspQVMeBmWI2AhxULWEeo7AaMC&index=25(15:00)

# How can timeouts make or break back-end load balancers
1. Incorrect timeouts can wreak havoc at the back-end. 
2. Recent AWS timeout, slack timeout occurred due to non-configurable timeouts. 

## Timeouts in context of load balancers
1. Reverse proxies accept connections on behalf of clients and route them to the appropriate back-end.
2. A proxy decides whether to keep the client connection or release the resources allocated to it and close it.
3. Inactivity is the client not sending anything for long periods of time. 
4. Another perspective is client is not actively sending anything but the proxy is currently is sending a large number of TCP packets as a part of say downloading a huge file. Each TCP packet needs to be acknowledged. There's is an acknowledgement timeout on behalf of the timeout for each packet. For ex: HAProxy has a send_timeout for this purpose. send_timeout should obviously be smaller and inactivity timeout should be larger.
5. Client is sending a very large file to a proxy. This is broken into multiple TCP packets and sent to the proxy. The client sends one TCP packet then waits for acknowledgement till the inactivity timeout time. One it gets an acknowledge it sends another packet. The server waits for the whole response to come. There is a Loris attack that is based on this principle which leads to the consumption of too many resources on the proxy's end. There's a timeout for the entire logical HTTP request at the proxy's end i.e. how much time in this case it will take for the entire file to be uploaded and assembled at the proxy's end.
6. Keep alive: How long should the HTTP connection should be kept alive?  TCP itself has a keep live concept. 

## Back-end aspects of timeouts.
1. Proxy calls a fleet of back-end servers to service the request. Which server to hit depends on the load balancing algorithm.
2. We must have pre-heated / pre-genarated TCP connection for each back-end server.
3. What if the server does'nt respond to the TCP SYN-SYN-ACK? That's one timeout we can configure on the proxy's end. How long should the client as a proxy wait? 