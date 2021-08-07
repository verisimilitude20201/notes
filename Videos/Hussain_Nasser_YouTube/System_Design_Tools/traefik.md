Video: https://www.youtube.com/watch?v=C6IL8tjwC5E&list=PLQnljOFTspQVMeBmWI2AhxULWEeo7AaMC&index=18

# Traefik - A reverse/forward proxy
1. Open source reverse proxy written in Go and marketed as a an Edge router.
2. Supports TLS

## Current architecture
1. Single server may go down servicing client requests
2. Insert a reverse proxy like Traefik and increase the number of back-end servers and load balance the requests to multiple back-ends.
3. Components
   - Providers (file, k8, docker): Kinda plugin that is feeded a configuration. Supports many providers
   - Static Vs Dynamic configuration: Start traefik feeding it the static configuration and static configuration points to the dynamic configuration
   - Entrypoint (Frontend): Publicly aaccessible port/ip of the proxy
   - Service(Backed): Multiple back-end in the proxy configurations
   - Routers: Routers link entry point and services. If you directly link the entry point with services, changing the entry point might need to restart the services. Routers listen to all entry points. /app1, go to app1 services, /app2 go to app2 services
   - Middleware: Optional part sitting between the router and services to provide extra features like compression
4. Static configurations include entry points and resolvers.
5. Dynamic configurations include services, routers and middleware
6. Proxying mode
  - Front-end and backend can have a mode of control
  - Can use mode tcp becomes layer 4 proxy
  - Can use mode http to become layer 7 proxy

