Video: https://www.youtube.com/watch?v=2Nt-ZrNP22A&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=6(05:46)

# Websockets

1. Websockets is a bi-directional full-duplex communication protocol for communication with client and server over Web.
2. Standardized in 2011. This allows live chats, feeds, notifications, multiplayer gaming and such realtime applications


## HTTP - Why Web sockets? 
1. HTTP request response, without request there is no response.
2. After every request the connection is closed after servicing a response. HTTP/1.0 opens and closes connections for every resource.
3. HTTP/1.1 first request leaves the connection open unless all web elements of an index.html, JS,CSS are loaded by the browser.
4. Certain use-cases require realtime serving of data without the server requesting the data.