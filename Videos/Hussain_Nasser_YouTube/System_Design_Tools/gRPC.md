Video: https://www.youtube.com/watch?v=Yw4rkaTc0f8(1:03:00)

# gRPC

1. gRPC is an open source RPC protocol built by Google.
2. It uses HTTP/2 and Google protocol buffers as an underlying message format.


## Motivation

### Client Server communication
1. We had SOAP, REST in the 90s and the early 2000s.
2. SOAP is a sort of RPC. It relies on client and server having their own libraries that both understand the SOAP schema that describes the operations that can be called remotely and their input structure and response formats. It relied on a bloated XML (WSDL) that is transferred from client to server. It has interoperability problems.
3. Roy Fielding comes up with REST.  JSON became popular as the input and output formats. Don't care about schema. 
4. GraphQL was invented to reduce the number of network calls.
5. For Bidirectional communication, SSE and Websockets were invented. Websockets does'nt have a format, it justs sends bytes. SSE - Server sends events to client.
6. Most databases invent their own protocols over raw TCP and message formats. For eg. Redis uses RESP

### Problems with client libraries
1. Any communication protocol needs a client library for the language of choice. For example: for REST we need an HTTP library, SOAP we need a SOAP library.
2. Maintaining and patching client library need extra work (HTTP/1, HTTP/2, security features). This makes sense if you are making calls in a programming language application.

### Why was gRPC invented
1. Every time we invent something new protocol, we have a new client library. gRPC is one library for popular languages all maintained by Google.
2. Protocol: HTTP/2 is used internally. If in future, Google changes this to HTTP/3, this change is completely transparent to the consumers of gRPC
3. Because of Protocol buffers, this is language agnostic.


## gRPC Modes
Below are the important modes of communication with gRPC

### Unary gRPC
1. Client makes a request to the server synchronously.
2. Server does some processing and sends the response back

### Server Streaming
1. Client makes one request to the server
2. Server responds with an infinite stream of data to the client.
3. For example: Youtube video streaming

### Client Streaming
1. Client sends a huge stream of data for example uploading a file

### Bidirectional Streaming
1. Both clients and server communicate with one another in a continous stream. For example: Multiplayer gaming and chatting. 

## Pros
1. Fast & Compact: Using Protocol buffers which is binary. HTTP/2 compresses even more. It's binary + compressed.
2. One client library per language. Very similar library maintained by Google or the community. Every security feature, patch we get it. REST has many many client libraries.
3. Server side streaming can be used to give progress feedback for uploads.
4. With HTTP/2 you can send a CANCEL request. HTTP/2 associates a request id with each request and the request. HTTP/1.1 cannot cancel request. Client can cancel but request will be churning on the server.
5. Benefits of HTTP/2 (Push notifications, Streaming, Compression) and Protocol Buffers automatically comes.

## Cons
1. Forcing to use a Schema is a con. In the days of REST, no need to maintain a schema just send the data and consume whatever components from the response that we can.
2. Thick client
3. Proxies: Layer 7 proxies cannot look into the data and take routing decisions. Nginx can understand gRPC. Layer 4 proxies are simpler.
4. Error handling is a pain, does'nt use raw HTTP codes.
5. No native browser support. Debugging is so hard.
6. Timeouts (pub/sub): Request-response model waits for results. We need to manage the timeouts for the gRPC-linked microservices. Pub-Sub decouples the client and server. gRPC is a form of synchrounous communication only.

## I'm tired of new protocols - Why spotify moved to gRPC
1. Spotify built Hermes their own protocol
2. Every new employee had to learn Hermes and they faced interoperability problems while communicating with outside services.