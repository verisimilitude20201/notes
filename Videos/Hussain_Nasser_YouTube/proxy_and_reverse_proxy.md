Video: https://www.youtube.com/watch?v=SqqrOspasag&list=PLQnljOFTspQUNnO4p00ua_C5mKTfldiYT&index=12

# Proxy and Reverse Proxy


1. Proxy makes requests on behalf of a client. Any request that goes out of the machine get established through the proxy. For example: I want to google.com via my-proxy.com. Any request that goes out goes through the proxy.
2. For google.com, the client is the proxy
3. Use-cases 
   - ISPs use proxies to block unwanted websites. 
   - Organizations too use proxies to block unwanted content
   - Caching web content
   - Anonymitity from the final destination: Google.com knows only the proxy server's IP and not the IP.
   - Logging requests
   - Sidecar proxies used in Microservices and have it take care of networking stuff like upgrading the protocol to HTTP/2 or HTTP/3

4. Reverse proxy client does'nt know the final destination. Proxy in this case communicates on the applications behalf to the server. Often used for load balancing

                                                                                                                                                        google-server-1 
        
        Client wishing to go to google.com ==> Reverse proxy (google.com) ==>
                                                                                    google-server-2

5. Reverse proxy use-cases
    - Caching 
    - Load balancing
    - Ingress: Used in Kubernetes. Routes requests to microservices based on URI paths
    - Canary Deployment: Canary deployments are a pattern for rolling out releases to a subset of users or servers. The idea is to first deploy the change to a small subset of servers, test it, and then roll the change out to the rest of the servers
    - Sidecar proxy

6. Examples of reverse proxy: HAProxy, Nginx, Istio, Envoy

7. Can proxy and reverse proxy be used at the same time
   - Service Mesh can use this.

8. Can I use proxy instead of VPN for anonymity: VPNs is more secure than proxy because at a lower level. Only thing that VPN can see in the content is the domains.TLS-terminating proxy can see everything.

9. Is proxy for HTTP Proxy? No. There is SOCKS proxy, HTTP proxy, Streaming Proxy, TLS termination proxies, Gopher proxy, FTP proxy, Streaming RTSP proxies and so on.