# Reverse Proxy Vs Load balancer

## Reverse Proxy
1. Reverse proxy hides the back-end infrastructure. Client connects to the reverse proxy instead of directly connecting to the back-end servers.
2. Client does not know exactly which back-end server it connects to.
3. Certain applications
  - Caching Web acceleration.
  - Canary deployments: For 10% of incoming requests, forward this to particular server that hosts new content for new users.
  - Security against external traffic. Internal network is isolated behind a firewall.
  - Singly entry URL.
  - Load balancer

## Load balancer
1. Just a special case of a reverse proxy. Balances the load.
2. Same as reverse proxy accepts the outside requests and distributes it across multiple back-end servers.
3. Might store some meta-data about server.