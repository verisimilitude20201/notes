Video: https://www.youtube.com/watch?v=hcw-NjOh8r0&list=PLQnljOFTspQWdgYcGXCTkjda8vd2jWJYt&index=2(50:00)

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

      }
   }

   events {

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