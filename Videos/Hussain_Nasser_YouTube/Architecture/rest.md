Video: https://www.youtube.com/watch?v=M3XQ6yEC51Q&list=PLQnljOFTspQXNP6mQchJVP3S-3oKGEuw9&index=1 (12:10)

# REST

1. Architecture that became immensely popular for designing web APIs.
2. It was a dissertation of Roy Fielding

## Representation
1. Back-end API represents way to manipulate and view a resource say users. The API gives the user in a representation (JSON/Protobuff). The back-end may store data in any format (CSV, Cassandra row, Oracle)
2. The API representation decouples the back-end storage representation. 

## State Transfer
1. Don't store state information on the server, all state information should be stored in a database on the server side.
2. Each request should include all information needed by the server to process the request for stateless requests. For stateful requests, a unique identifier is sent that identifies this user that associates a state over the previous requests that the user has sent.
3. Stateless requests are good for load-balancing.