Video: https://www.youtube.com/watch?v=hcw-NjOh8r0&list=PLQnljOFTspQWdgYcGXCTkjda8vd2jWJYt&index=2(1:35:03)

# Nginx

1. Web server written in C that can be used as a reverse proxy and as a load balancer.
2. Serves Web content. 
3. It acts as a proxy: 
    
    a. load balancing 
    
    b. backend routing: If you are going to /app1, go to these set of services, /app2 consume these set of resources of 20 services.
    
    c. caching

## NGinx architecture
   a. Consider a Web application listening to 3001 http port that saves to a database listening to 5432 by connecting to it.
   
   b. For a single instance, you can use the small http web server embedded in the application to interact with its REST APIs.
   
   c. As you scale, you increase the number of servers. It's not possible to share these many URLs with so many customers.
   
   d. To solve this, you put Nginx in the middle, enable https, add a certificate, configure it so that it load balances the set of services. We can do caching at the Nginx, add Varnish on top.
   
   e. Single endpoint that you can refer and you can scale the back-end independently.

## Layer 4 and Layer 7 load balancing
   a. Nginx can operate in layer 7 (http) or layer 4 (tcp)
   
   b. Stream context - TCP (layer 4 load balancing), Http context - Layer 7 load balancing

## Sample NGinx Web configuration
   1 http {
      1 server {
         1 listen 8080;
         3 root /var/www/html;
         4 location /images {
            root /var/www/images;
         }
         5 location ~ .jpg$ {
            return 403;
         }
      }

      server {
         listen 8888
         location / {
         6  proxy_pass http://localhost:8080/;
         }
      }
      7 upstream allbackend {
         8 ip_hash;
         server 127.0.0.1:2222
         server 127.0.0.1:3333
         server 127.0.0.1:4444
         server 127.0.0.1:5555
      }

      9 upstream app1backend {
         server 127.0.0.1:2222
         server 127.0.0.01:3333
      }

      9 upstream app2backend {
         server 127.0.0.1:4444
         server 127.0.0.1:5555
      }

      server {
         listen 80;
       7  location / {
            proxy_pass http://allbackend;
         }

         9 location /app1 {
            proxy_pass http://app1backend;
         }

         9 location /app2 {
            proxy_pass http://app2backend;
         }
         10 location /admin {
            return 403;
         }

      }
   }

   events {

   }

   11 stream {
      upstream allbackend {
            server 127.0.0.1:2222
            server 127.0.0.01:3333
            server 127.0.0.1:4444
            server 127.0.0.1:5555
      }
      server {
         listen 80
         proxy_pass http://allbackend;
      }
   }

1. Block directives are http and server. listen is a leaf directive.
2. Re-read Nginx config - `nginx -s reload` and then `nginx`
3. Different websites with different content - just create different folders within /var/www/html
4. To load images from a different directory at the same level of html folder. You cannot serve the whole images folder though.
5. Match URLs with regular expressions.
6. Redirect to http://localhost:8080 when someone tries to access http://localhost:8888. This is a redirection. Nginx is lazy loading, HAProxy is eager loading.
7. This load balances the request to 4 services running on 127.0.0.1 on ports 2222 to 5555.
8. Consistent Hashing: This hashes the client IP address and maps it to one of the services. Every time a certain client sends a request, it gets mapped to the same server IP. This is useful for stateful sessions because it sticks a client request to one server. Idea of having a sticky state in memory is not useful especially in case of Kubernetes where containers keep coming up and down.
9. Path based request routing.
10. Block /admin connections
11. Layer 4 proxy configuration: No path-based routing. This leads to 5 TCP connections in the above example: 4 connections with the 4 backend servers and 1 connection with the client. Can use any protocol that supports TCP. Most of them when you refresh you are redirecting to the same server. Browser can decide to hit another server. If we would do that with Telnet, it will we will find that it's almost round-robin


## Hosting DNS to the public server, add cert
1. Enable port forwarding on your router's public IP address to your internal computer/service's port 80/443.
2. Create a hostname on noip.com that points to your public IP
3. Use Let's encrypt certificate.
  - Install Let's encrypt certbot
  - Stop the nginx server
  - Generate standalone certificate and private key. It'll ask for domain and username
4. Configure nginx and enable TLS 1.3, http/2
   
         http {
            server {
               listen 80; 
               listen 443 ssl http2;
               ssl_certificate # Public Key PAth
               ssl_certificate_key # Private key path
               ssl_protocols TLSv1.3
            }
         }

5. TLSv1.2 is slow and uses ancient ciphers. 


## Nginx 6 timeouts
1. client_header_timeout: The timeout on Nginx's side that waits for all of client's request headers to be transferred.
2. client_body_timeout: The timeout on Nginx's side that waits for all of client's body  to be transferred. The client's body is split into packets and once one packet is received, the entire timeout gets reset. The body is actually large that's why.
3. send_timeout: When nginx is about to send the response to the client. The server's response is split into packets and once the client receives a packet and acks it, the timer gets reset.If a packet takes more than 60 seconds to transfer and client does'nt ack it within  60 seconds, the connection is broken.
4. keepalive_timeout: Timeout after which an idle connection will be closed.
5. lingering_timeout: When lingering_timeout is disabled, the connection will die immediately. If the client does'nt handle it gracefully, it leads to issues. When it's enabled, it starts a timeout when client requests a closure or server requests a closure. After the timer starts, it will get reset whenever a client tries to send a request. It can be used for cleaning up the data bytes in the socket.
6. resolver_timeout: How long we need to wait for Nginx to resolve a DNS.

## Nginx Backend timeout
1. proxy_connect_timeout: Nginx starts and tries to connect with the upstream back-end servers. If Nginx cannot connect to the backend supposed to be in the same network within 60s, it closes the connection
2. proxy_send_timeout: The time it takes for Nginx to send a request to the backend server. This is split into packets and if for a single packet the backend server doesn't ack within 60 seconds, the connection is closed.
3. proxy_read_timeout: Nginx successfully sent the GET request to the server. Server starts sending the response made up of 3 packets. Nginx receives that packet and reads it and acks it and the timer is reset. The server transfer second packet. If any read operation takes more than 60 second when a packet is sent, this time out kicks in and Nginx closes the connection
4. keepalive_timeout: Closes all idle connections beyond this timeout.Should be kept higher because establishing a connection is expensive.
5. proxy_next_upstream_time: how long should be keep trying reaching upstream servers if any of the servers fail to accept a connection. After this timeout is reached, the NGinx responds with a 503 Service not available.