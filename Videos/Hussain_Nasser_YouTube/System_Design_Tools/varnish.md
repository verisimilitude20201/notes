Video: https://www.youtube.com/watch?v=-cWs6eoyaLg

# Varnish - HTTP accelerator

1. Varnish is a reverse proxy web accelerator designed to improve the Web access times. 
2. It is written in C.

## Classic HTTP architecture
1. A single HTTP web server listening on port 2015 and it interacts with a PostgreSQL database on 5432 port.
2. A GET /employees will hit the Web server and Web server hits the database to fetch the employees even if the employees did not change. This is Expensive.

## Enter Varnish
1. Varnish accepts the GET /employees first time. It reaches out to the Web server, which in turn calls the database. Web server relays back the JSON list of employees to Varnish
2. From the second request onwards, Varnish caches this response and serves it directly instead of talking to the Web server. 
3. Varnish is a Layer 7 reverse proxy. It has to look at the data to decide whether it should cache it.
4. By default it caches GET requests, prefetches documents. If you request index.html, you will also need main.css, main.js and so on.
5. Anything behnind Varnish is back-end and anything before Varnish is called front-end.
6. To support HTTPS, you can use an Nginx before Varnish as a reverse proxy and use it as a TLS terminator. Or we can use a Caddy Web server.
7. Varnish does not support HTTPS backend in the open source but it has a paid feature that supports HTTPS backends.

## Varnish Pros
1. Cache and prefetching documents work naturally (Developer responsible for caching in other caching systems like Redis). This can be a double-edged sword. That's why people prefer Redis, I want to cache stuff and invalidate cache my way.
2. Resolves the DNS hostnames in the response content. It remembers the DNS's IP address.
3. Rewrite scripts to optimize code. Like writing a JavaScript in an optimal way to give the same output.
4. Load balancing. Only supports HTTP.
5. Backend connection pooling of TCP connections to the back-end. Max size of the connection pool is configurable.
6. Varnish Modules: For example: Rewriting headers, add other headers, enable POST request caching
7. Edge side includes: ESI markup language explicitely marks the dynamic content. Varnish caches the remaining page and explicitely fetches the dynamic content on the cached page.

## Varnish Cons
1. Cache invalidation algorithms are expensive and difficult to solve. Redis has more control over this.
2. Only works on unencrypted versions. The open source version.
3. For HTTP Front-end, terminate TLS so that you can unencrypted stuff to Varnish.
4. HTTPS backends not supported in open source Varnish
5. Can't cache POST requests. (e.g GraphQL queries). GraphQL sends a huge payload of a query so it uses POST only for normal queries. We can use a module to cache GraphQL POST requests. But it's difficult, how would we know what requests to cache? That's why people prefer Redis because it offers much more control over what should be cached and what should'nt
6. Varnish does not support HTTP/2 very well. 