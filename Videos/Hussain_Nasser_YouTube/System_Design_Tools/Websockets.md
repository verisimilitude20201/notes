Video: https://www.youtube.com/watch?v=2Nt-ZrNP22A&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=6(25:00)

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