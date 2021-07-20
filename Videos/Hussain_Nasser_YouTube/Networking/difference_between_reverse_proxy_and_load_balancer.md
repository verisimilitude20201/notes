
# Difference between reverse proxy and a load balancer

# Reverse proxy
1. Reverse proxy is a proxy that receives the client request and hides the back-end servers that actually process it and service that request.
2. Client does'nt know which backend is actually servicing the request.
3. Applications:
  - Caching Web content and directly serve cached content from the proxy itself.
  - Canary deployments: Enable a feature for only a subset of users.
  - Security against external traffic. Can avoid SSL handshake between the internal network
  - Single Entry URL
  - Make the reverse proxy as a load balancer


## Load balancers
1. Load balancer is just one instance of a load balancer
2. Load balancer needs more than one servers to re-route the requests. Reverse proxy can have a single server.
3. There are many algorithms for load balancing and certain algorithms need to be aware of the content of the request.