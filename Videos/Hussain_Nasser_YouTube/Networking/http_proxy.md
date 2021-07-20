Video: 

# HTTP Proxy

## Transparent Proxy

1. Most ISPs use it. 
2. Transparent proxy sits at the ISP level, intercepts requests, applies filtering (mostly ip-address based) to determine if you can go to that destination and will block it if so. 
3. This is exactly a kinda gateway to the outside world.
4. One TCP connection with packet switching.
5. This is transparent, may not be explicitely configured on client machine.
6. Only looks at layer3/layer 4 (IP and Port only)


## HTTP Proxy
1. Here the client is actually aware of the proxy and explicitely configured to use the proxy.
2. Here the TCP packet from the client is destined to the proxy server that's configured
3. At the proxy, the Host header is inspected and a fresh connection is initiated between the proxy and google.com server. Host header was added to HTTP in 1.1. 
4. Google returns the result to proxy and proxy returns the content to the client

## HTTP Proxy Pros
1. Two TCP connections
2. Looks through the content. Looks at Host header
3. Can change the content. Can optionally add X-FORWARDED-TO that contains the client's IP address.
4. Used in service meshes. Linkerd allows request of the following sort http://service1/ where service1 does not exist.
5. Linkerd delivers you different services based on the hostname. ServiceMesh does like that. It queries the service discovery, finds the IP and redirects the connection there.