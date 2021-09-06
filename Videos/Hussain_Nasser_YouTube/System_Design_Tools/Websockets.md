Video: https://www.youtube.com/watch?v=2Nt-ZrNP22A&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=6(41:00)

# Websockets

1. Websockets is a bi-directional full-duplex communication protocol for communication with client and server over Web.
2. Standardized in 2011. This allows live chats, feeds, notifications, multiplayer gaming and such realtime applications
3. Use-cases
  - Chatting
  - Live Feed: Facebook feed
  - Multi-player gaming
  - Show client progress/logging: While uploading a large file. 


## HTTP - Why Web sockets? 
1. HTTP request response, without request there is no response.
2. After every request the connection is closed after servicing a response. HTTP/1.0 opens and closes connections for every resource.
3. HTTP/1.1 first request leaves the connection open unless all web elements of an index.html, JS,CSS are loaded by the browser.
4. Certain use-cases require realtime serving of data without the server requesting the data.

## What are websockets
1. Uses HTTP/1.1 as a vehicle from the client to server and server to client. 
2. Server is aware of the client so it's a stateful thing.
3. Initially an HTTP request is sent along with a Websocket handshake if the client and server support.
4. It's a full duplex connection, anyone can send data to each other.
5. The Handshake
  - First request is a normal HTTP GET request with an Upgrade Header set to Websockets
  - Server responds with 101 (Switching protocols) if it supports Websockets.
  - Few random strings are exchanged between the client and server just to verify that server is the same that the client first send the request to.


## Pros
1. Full duplex. Polling not required if the client needs a server to send updates whenever they are available.
2. HTTP Compatible because of the presence of the Upgrade header.
3. Firewall friendly. Because it's HTTP either 80 or 443.

## Cons
1. Proxying is tricky. Nginx just started supporting Web socket traffic. Layer 7 proxy terminates the TLS connection and so there will be two connections between client and proxy and proxy and server
2. L7 load balancing timeouts challenging. The connection should stay open for longer periods of time.
3. Stateful, difficult to horizontally. Can persist the connection IDs to a database

## Do you have to use Websockets ? 
1. If bi-directional communication is needed?
2. Long polling: Client makes a request and can wait until a server has the information. Kafka uses long-polling. Problem with Web sockets is server does'nt know that the client has disconnected.
3. EventSource: Server pushes information all the time like notifications. Most Websockets use-cases we can solve with Event Source