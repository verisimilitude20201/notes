# HTTP 2.0 Load balancing


1. There can be HTTP/2 on the front end between the client and the load balancer and between the load balancer and the back-end
2. Assume that a client and load-balancer uses HTTP/2.0
   - Client initiates a GET /index.html
   - A single connection sets up between the load balancer and the client
   - 3 Separate streams of data - index.html, it's CSS and JavaScript are sent as bytes through the stream.
   - Load balancer's TCP component assembles the packets belong to the 3 types of request streams.
   - Since it's round-robin, the load balancer sends the first request (GET /index.html) to the first back-end server. Since back-end server uses HTTP/1.0, there are 3 separate TCP connection. 
   - The CSS request and JS request are send as separate TCP connections to separate servers.
   - Load balancer assembles response packets from the 3 back-ends and sends them to the client. If the load-balancer is a TLS-terminating proxy, it will decrypt the back-end response, assemble it and encrypt it and send it back to the client.

3. If all is HTTP/2, a single TCP connection gets created between the back-end and a single server and all index.html, JS, CSS get sent as 3 separate streams within the same TCP connection with the same backend server.
4. What if 