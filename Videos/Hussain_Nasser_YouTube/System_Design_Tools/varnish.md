Video: https://www.youtube.com/watch?v=-cWs6eoyaLg(22:00)

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