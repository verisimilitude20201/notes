Video: https://www.youtube.com/watch?v=hcw-NjOh8r0&list=PLQnljOFTspQWdgYcGXCTkjda8vd2jWJYt&index=2(15:00)

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