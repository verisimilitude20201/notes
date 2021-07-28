Video: https://www.youtube.com/watch?v=g7WeY0DZNJ0&list=PLQnljOFTspQVMeBmWI2AhxULWEeo7AaMC&index=14 (10:32)

# Sidecar proxy
Application design pattern that abstracts certain network communication such as security, monitoring, retries, timeouts and protocols such as GRPC away from the main architecture of the applications.

## Library Pattern
1. Add a library to your application and start consuming functions from it.
2. Imagine an appliation 
 - Server-side node.Js application that supports REST APIs to interact with a stored data. Server side application needs libraries for database interaction and supporting REST. 
 - At the client side, there is a Java application that communicates with the Node.js server via REST. It just requires a REST client side library for doing the REST requests. 
 - We add new features of timeouts, we don't want the requests to take forever to execute. Because we add timeouts, we add retries, circuit breaking, HTTP/2. 
 - For all this, we start adding extra code to handle retries, timeouts, circuit breaking, HTTP/2. 
 - We also add similar code for server. 
 - We basically add code to handle networking level stuff to both the client and the server. We are wasting your time on building networking stuff rather than focusing on application logic.
 - If we need to add a new client, we would need to redo this thing for that client as well.

 Enter Sidecar proxy

 ## Sidecar proxy
 1. Separate all networking functionalities into separate bubbles that uses proxies. Run this proxy on the same localhost and port that your application listens to. 
 2. Configure your application so that all layer 7 HTTP requests use this proxy. Any request will first go to the proxy, the proxy will forward the request to the microservice.
 3. The communication between client and proxy can be HTTP/1.1
 4. The same sidecar proxy can be set as the reverse proxy on the back-end side. 
 5. The server-side sidecar proxy forwards the request to the back-end and back-end returns the response back to the server side proxy from where it goes to the client-side forward proxy which further relays it to the client-side application
 6. We could use this to change even the response types. For example: Back-end services can return JSON which is converted to Protocol Buffers which is sent over the network to the client-side proxy.
 7. The proxy and other application can be on the same host and can communicate with each other via localhost interface.

## Examples of Sidecar proxy
1. Service Mesh: Linkerd(can use any language you want), Istio(Google), Envoy(Lyft)
2. Finagle by Twitter
3. Sidecar Proxy container: Proxy spinned up as a container
4. Must be layer 7 proxy.

## Pros
1. Language Agnostic (Polyglot). Because the networking layer is separately. Make a simple HTTP/1.1 request to the sidecar proxy.
2. Protocol upgrade: Upgrade an HTTP/1.1 request to HTTP/2.0
3. Security: Authentication can be implemented in the proxy itself. Istio/Linkerd have configurations that stop communicating a service to another.
4. Tracing and Monitoring: Log all requests. Measure tail latencies, show a nice dashboard
5. Service Discovery
6. Caching

## Cons
1. Complexity: Prone to failures, latencies the moment you break a library pattern into a network call for a sidecar proxy.
2. Latency: Networking deals with latency. UDP might solve this problem being a connection-less system.