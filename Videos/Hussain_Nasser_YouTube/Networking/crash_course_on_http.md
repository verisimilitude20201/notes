Video: https://www.youtube.com/watch?v=0OrmKCB0UrQ&list=PLQnljOFTspQUNnO4p00ua_C5mKTfldiYT&index=7

# Crash Course on HTTP

## HTTP and HTTPS
1. HTTP is a protocol for transferring hypermedia (text, media, files, videos, binary files) over the web.
2. Layer 7 protocol.
3. TCP is the vehicle that transports HTTP requests/responses.
4. 3-Way handshake: to open a TCP connection, data gets converted into bytes gets sent to server. Server sends another set of packets as a data response.
5. HTTPS involves a TLS handshake between client and server to have the same shared session key for the encrypted communication that cannot be eavesdropped upon.

## Client-Server
1. (Client) Browser, Python, javascript app or any app that makes an HTTP request.
2. (Server) HTTP Web server provides response to the request.


## HTTP Request
1. Has a URL: Path to a resource on the Web
2. Method Type: GET/HEAD/POST/PUT/DELETE each offering different semantics of manipulating a resource
3. Headers: Cache headers, content negotiation, location, host and so on.
4. Body: Only POST and PUT have a body.

## HTTP Response

1. Status Code: How did the request fare at the server's end.
2. Headers: Content type, encoding type
3. Body: Actual content of the requested resource.

## 1996 - HTTP 1.0

1. Establishing a TCP connection required a lot of RAM. Immediately close the connection after the response is done
2. The content involved just static Web pages. No concept of hypermedia like videos. 
3. The number of different contents on a Web page involved multiple separate HTTP web requests to the server which are immediately closed after they are delivered.
4. Typically slow
5. Response buffering: Huge index.html is sent slowly to the service.

## HTTP 1.1 - 1997
1. Only protocol that servived 22 years
2. Persisted TCP connection: Invented Keep-Alive header to transfer all contents of a web page in the same connection. Both clients and server maintain the same connection.
3. Introduced caching with ETags
4. Low latency
5. Streaming with Chunked transfer: Send data as soon as it comes.
6. Pipelining (Head of line problem where clients keep waiting about requests.) Pipelining means send all requests at once and then send all responses

## HTTP/2 SPDY

1. Multiplexing: Multiple requests come and shove it into one channel/connection.
2. Compression: Supports Google Protocol Buffers
3. Server Push: Server pushing events. Browser does'nt pull.
4. All protocols are stateful, because underneath TCP is stateful but it appears that it's stateless
5. Always https by default
6. Protocol negotiation during TLS (Next Protocol Negotiation/Application Layer protocol negotiation) that negotiates the version of http. ALPN is an extension of TLS.

## HTTP/2 over QUIC Quick UDP Internet Connection (HTTP/3)

1. Replaces TCP with UDP with Congestion control
2. All HTTP/2 features