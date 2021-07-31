Video: https://www.youtube.com/watch?v=PAJ5kK50qp8 

# HTTP Connect
HTTP Connect is an important component of tunneling and proxying

## Advantages of HTTP Proxy
1. Client wants to connect to a server beyond it's reach.
2. Client does'nt know how to talk to a target server. This is an advantage of a ServiceMesh in a micro-service architecture. Microservice uses HTTP/1.1 but does not know how to talk HTTP/2.0
3. Proxy has access to a server and knows how to talk to it.
4. Client connects to a proxy and proxy connects to a server on behalf of a client.

## HTTPS Proxy
1. Decrypts the content between the client and the proxy. 
2. It serves its certificate to the client instead of the target server. Proxy sees everything (good for debugging web traffic E.g Fiddler to see REST calls to a system)
3. How HTTPS proxy works
  - Client sends a TLS Client hello to the proxy server.
  - Proxy responds with a TLS server Hello.
  - Proxy establishes a TLS Client hello to the server
  - Server responds with a TLS server hello and it's certificate

## HTTP Connect
1. Creates a tunnel between the client and target server
2. Client sends a HTTP Connect to proxy containing the target server 
3. PRoxy creates a TCP connection to target server
4. Once successful, proxy returns success to client
5. Now, any packet the client sends goes as-is to the target server. This includes TLS handshake establishing e-to-e
6. Example: 
   - Client machine (IP 1.2.3.4), Proxy 9.9.9.9 and example.com:443, IP 5.5.5.5
   - Client sends a packet to the proxy destined for the server.
   - Proxy sees the packet and initiates a TCP connection with the server. It changes the target IP address of the server and the port. 
   - Server responds with a TLS server hello to proxy. Proxy understands that this is for the client and simply forwards it to the client.
   - Proxy acts just as a relay for IP packet traffic.
   - Only way if the proxy can look at the content is if it responds with it's own certificate or it fakes the server's certificate.

## HTTP Connect Chaining
Similar concept extended between client, server and one or more proxies in between.


## HTTP Connect Pros
1. Connect to secure servers without proxies having to be TLS terminating.
2. Supports protocols not normally supported through proxies (WebSockets, WebRTC). So the proxy can tunnel Websockets traffic even if it does'nt understand Websockets protocol. This can be used to smuggle content across proxies because it's layer 7 proxies
3. Proxies can't read encrypted traffic.

## HTTP Connect Cons
1. Only supports TCP, can't proxy UDP traffic 
2. Each CONNECT opens a new TCP connection to the same target (no multiplexing). There's no HTTP/2 connect either. Cisco implemented that though.
3. Bad implementation would allow to tunneling to port 25 to send spam emails.. RFCs recommends to allow HTTP connect to only ports that makes sense.

## Multiplexed Application Sustrate over QUIC Encryption (MASQUE)
1. Allows UDP tunneling. 
2. Allows multiplexing
3. Allows only certain ports for tunneling.