Video: https://www.youtube.com/watch?v=JhpUch6lWMw&list=PLQnljOFTspQUNnO4p00ua_C5mKTfldiYT&index=8

# Web Servers

1. Web server is a server that servs Web content through the HTTP protocol. They can serve static & dynamic web content, API
2. Out-of-the-box Web servers - Apache, Tomcat and Web servers supported in languages such as Python web.py


## What is a Web server

1. Software that serves web content (PDF, Videos, APIs, hypermedia, textual content)
2. Uses HTTP protocol
3. Both static and dynamic content like web pages, blogs, build APIs
4. Static HTML files that can be hosted on a Web server and can be cached. These can be shared amongst users by sharing URLs instead of actual files.
5. Dynamic content is produced by fetching data from the database and presenting it according to the logged in user, location and so on. Web 1.0 everything static, Web 2.0 interactive web.

## How do Web Servers work?

1. Client (44.1.1.1) requests a page from a web server example.com:80
2. GET /index.html
3. Server sends back 200 OK with index.html along with headers and status code.
4. Client initiates a TCP connection which acts as a vehicle for HTTP request/response with the server on port 80. A TCP socket is created for that client in memory on the Web Server. And tries to fetch the index.html from the local disk and serves it over the socket. 
5. If more than one connection is initiated, the Apache server spins another thread that initiates a new socket to serve the client.
6. The Web server has a maxThreads parameters that designates the number of threads to spin at a time to serve multiple clients in a concurrent manner.